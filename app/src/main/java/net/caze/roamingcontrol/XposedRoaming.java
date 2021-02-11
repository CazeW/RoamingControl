package net.caze.roamingcontrol;
import static de.robv.android.xposed.XposedHelpers.*;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.ServiceState;
import android.os.Build;
import android.telephony.TelephonyManager;

//import de.robv.android.xposed.XC_MethodReplacement;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import android.telephony.Rlog;
//import android.telephony.SubscriptionManager;

public class XposedRoaming implements IXposedHookLoadPackage, SharedPreferencesValues {
    //final private String FILEFOLDER = "/data/" + PACKAGE + "/files";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.android.providers.telephony")) {
            return;
        }

        final Method getprop = findMethodExact("android.os.SystemProperties", null, "get", String.class, String.class);
        final Context moduleContext =  AndroidAppHelper.currentApplication();

        String methodToHook;
        if (Build.VERSION.SDK_INT >= 24) methodToHook = "com.android.internal.telephony.ServiceStateTracker";
        else methodToHook = "com.android.internal.telephony.gsm.GsmServiceStateTracker";

        findAndHookMethod(
            methodToHook,
            lpparam.classLoader,
            "pollStateDone",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ServiceState newSS;
                    TelephonyManager tel = (TelephonyManager) moduleContext.getSystemService(Context.TELEPHONY_SERVICE);
                    int phone = 0;

                    try {
                        if (Build.VERSION.SDK_INT >= 18) // Android 4.3+
                            newSS = (ServiceState) getObjectField(param.thisObject, "mNewSS");
                        else
                            newSS = (ServiceState) getObjectField(param.thisObject, "newSS");
                    } catch (NoSuchFieldError e) {
                        XposedBridge.log(TAG + ": newSS not found");
                        return;
                    }

                    String prop = "gsm.sim.operator.numeric";
                    try {
                        // MTK dual SIM phones have mSimId and gsm.sim.*.2
                        int mSimId = getIntField(param.thisObject, "mSimId");
                        if (mSimId != 0)
                            prop = "gsm.sim.operator.numeric.2";
                    } catch (NoSuchFieldError ignored) {}

                    /*if (!newSS.getRoaming() && !force && !type.equals("Whitelist"))
                        return; // Android thinks we're not roaming - who are we to argue?*/

                    String sim;
                    if (Build.VERSION.SDK_INT >= 22) {
                        Object mPhone = getObjectField(param.thisObject, "mPhone");
                        phone = (int) callMethod(mPhone, "getPhoneId");
                        //sim = !tel.getSimOperatorNumericForPhone(phone).equals("") ? tel.getSimOperatorNumericForPhone(phone) : (String) getprop.invoke(null, prop, "");
                        sim = tel.getSimOperatorNumericForPhone(phone);
                        if (sim.equals("")) {
                            //XposedBridge.log(TAG + ": SIM operator not found for SIM" + String.valueOf(phone+1));
                            return;
                        }
                    }
                    else
                        sim = !tel.getSimOperator().equals("") ? tel.getSimOperator() : (String) getprop.invoke(null, prop, "");
                        //sim = (String) getprop.invoke(null, prop, "");

                    if (sim.equals("")) {
                        XposedBridge.log(TAG + ": SIM operator not found");
                        return;
                    }

                    //Read preferences
                    XSharedPreferences pref = new XSharedPreferences(PACKAGE, PREFNAME);
                    Boolean force = pref.getBoolean(KEY_PREF_FORCE, false);
                    Boolean national = pref.getBoolean(KEY_PREF_NATIONAL, false);
                    Boolean saved = pref.getBoolean(KEY_PREF_SAVED_ROAMING, false);
                    Boolean matchname = pref.getBoolean(KEY_PREF_MATCH_NAME, false);
                    String type = pref.getString(KEY_PREF_ROAMING_TYPE, NETWORK_VALUE);
                    Boolean whitelist = pref.getBoolean(KEY_PREF_WHITELIST_MODE, false);
                    Boolean advancedforce = pref.getBoolean(KEY_PREF_ADVANCED_FORCE, false);
                    Boolean dualsim = pref.getBoolean(KEY_PREF_DUALSIM, false);


                    ArrayList saved_networks = new ArrayList<>();
                    ArrayList force_saved_networks = new ArrayList<>();

                    Context context;
                    try {
                        context = AndroidAppHelper.currentApplication().createPackageContext(PACKAGE, Context.CONTEXT_IGNORE_SECURITY);
                    } catch (PackageManager.NameNotFoundException e) {
                        XposedBridge.log(TAG + ": Xposed could not get package context");
                        return;
                    }

                    if (dualsim){
                        national = phone == 0 ? pref.getBoolean(KEY_PREF_NATIONAL_SIM1, false) : pref.getBoolean(KEY_PREF_NATIONAL_SIM2, false);
                        if (!advancedforce)
                            force = phone == 0 ? pref.getBoolean(KEY_PREF_FORCE_SIM1, false) : pref.getBoolean(KEY_PREF_FORCE_SIM2, false);

                        if (phone == 0 && saved) {
                            if (type.equals(COUNTRY_VALUE))
                                saved_networks = readFile(context, COUNTRY_FILENAME_SIM1);
                            else
                                saved_networks = readFile(context, NETWORK_FILENAME_SIM1);

                        } else if (phone == 1 && saved){
                            if (type.equals(COUNTRY_VALUE))
                                saved_networks = readFile(context, COUNTRY_FILENAME_SIM2);
                            else
                                saved_networks = readFile(context, NETWORK_FILENAME_SIM2);
                        }

                        if (phone == 0 && force && advancedforce)
                            force_saved_networks = readFile(context, FORCE_FILENAME_SIM1);
                        else if (phone == 1 && force && advancedforce)
                            force_saved_networks = readFile(context, FORCE_FILENAME_SIM2);
                    }
                    else {
                        if (type.equals(COUNTRY_VALUE))
                            saved_networks = readFile(context, COUNTRY_FILENAME);
                        else
                            saved_networks = readFile(context, NETWORK_FILENAME);

                        force_saved_networks = readFile(context, FORCE_FILENAME);
                    }

                    Set<String> baseMCClist = new TreeSet<>(pref.getStringSet(BASE_MCC_LIST, new TreeSet<String>()));

                    String networkNameShort = newSS.getOperatorAlphaShort();
                    String networkNameLong = newSS.getOperatorAlphaLong();
                    String networkNumeric = newSS.getOperatorNumeric();

                    if (networkNameShort == null || networkNameLong == null || networkNumeric == null) {
                        //XposedBridge.log(TAG + ": Operator networkNameShort, networkNameLong or networkNumeric is empty");
                        return;
                    } else {
                        networkNameShort = networkNameShort.toLowerCase();
                        networkNameLong = networkNameLong.toLowerCase();
                    }

                    //National roaming
                    if (national) {
                        if (sim.substring(0, 3).equals(networkNumeric.substring(0, 3))) {
                            newSS.setRoaming(false);
                            newSS.setDataRoamingFromRegistration(false);
                            XposedBridge.log(TAG + ": National roaming set successfully for SIM" + String.valueOf(phone+1));
                        }
                        if (newSS.getRoaming()) {
                            for (String list : baseMCClist) {
                                if (list.contains(sim.substring(0, 3)) && list.contains(networkNumeric.substring(0, 3))) {
                                    newSS.setRoaming(false);
                                    newSS.setDataRoamingFromRegistration(false);
                                    XposedBridge.log(TAG + ": National roaming set (baseMCClist) successfully for SIM" + String.valueOf(phone + 1));
                                    break;
                                }
                            }
                        }
                        /*if (newSS.getRoaming())
                            XposedBridge.log(TAG + ": National roaming was not set for SIM" + String.valueOf(phone+1) + "(SIM MCC: " + sim.substring(0, 3) +
                                    " NET MCC: " + networkNumeric.substring(0, 3) + ")");*/
                    }
                    //Force roaming
                    if (force) {
                        if (advancedforce) {
                            if (!force_saved_networks.isEmpty()) {
                                for (Object obj : force_saved_networks) {
                                    Network network = (Network) obj;
                                    if (network.getNetwork().equals(networkNumeric)) {
                                        newSS.setRoaming(true);
                                        newSS.setDataRoamingFromRegistration(true);
                                        XposedBridge.log(TAG + ": Advanced forced roaming set succesfully for SIM" + String.valueOf(phone + 1));
                                        return;
                                    }
                                }
                            }

                        } else if (whitelist || (sim.substring(0, 3).equals(networkNumeric.substring(0, 3)) && !sim.substring(3).equals(networkNumeric.substring(3)))) {
                            newSS.setRoaming(true);
                            newSS.setDataRoamingFromRegistration(true);
                            XposedBridge.log(TAG + ": Forced roaming set successfully for SIM" + String.valueOf(phone+1));
                        }
                        /*if (!newSS.getRoaming())
                            XposedBridge.log(TAG + ": Forced roaming was not set for SIM" + String.valueOf(phone+1));*/
                    }
                    //Saved roaming
                    if (saved && !saved_networks.isEmpty()) {
                        if (type.equals(COUNTRY_VALUE)) {
                            for (Object obj : saved_networks) {
                                Country country = (Country) obj;
                                if (country.getMcc().equals(networkNumeric.substring(0, 3))) {
                                    newSS.setRoaming(false);
                                    newSS.setDataRoamingFromRegistration(false);
                                    XposedBridge.log(TAG + ": Country saved roaming set successfully for SIM" + String.valueOf(phone + 1));
                                    return;
                                }
                                for (String list : baseMCClist) {
                                    if (list.contains(country.getMcc()) && list.contains(networkNumeric.substring(0, 3))) {
                                        newSS.setRoaming(false);
                                        newSS.setDataRoamingFromRegistration(false);
                                        XposedBridge.log(TAG + ": Country saved roaming set (baseMCClist) successfully for SIM" + String.valueOf(phone+1));
                                        return;
                                    }
                                }
                            }
                            //XposedBridge.log(TAG + ":  Country saved roaming was not set for SIM" + String.valueOf(phone+1));
                        }
                        else {
                            for (Object obj : saved_networks) {
                                Network network = (Network) obj;
                                if (network.getNetwork().equals(networkNumeric)) {
                                    newSS.setRoaming(false);
                                    newSS.setDataRoamingFromRegistration(false);
                                    XposedBridge.log(TAG + ": Network saved roaming set successfully for SIM" + String.valueOf(phone+1));
                                    return;
                                } else if (matchname && network.getMcc().equals(networkNumeric.substring(0, 3))) {
                                    if (networkNameShort.contains(network.getName().toLowerCase()) || networkNameLong.contains(network.getName().toLowerCase()) ||
                                                network.getName().toLowerCase().contains(networkNameShort) || network.getName().toLowerCase().contains(networkNameLong)) {
                                        newSS.setRoaming(false);
                                        newSS.setDataRoamingFromRegistration(false);
                                        XposedBridge.log(TAG + ": Network saved roaming (name match) set successfully for SIM" + String.valueOf(phone+1));
                                        return;
                                    }
                                }
                            }
                            //XposedBridge.log(TAG + ":  Network saved roaming was not set for SIM" + String.valueOf(phone+1));
                        }
                    }
                }
            }
        );
        XposedBridge.log(TAG + ": hooked + " + methodToHook + ".pollStateDone");
        /*
        Class<?> NetworkSelectMessage = findClass("com.android.internal.telephony.PhoneBase$NetworkSelectMessage", null);
        findAndHookMethod(
                "com.android.internal.telephony.PhoneBase",
                lpparam.classLoader,
                "updateSavedNetworkOperator", NetworkSelectMessage,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        String NETWORK_SELECTION_KEY = (String) getStaticObjectField(param.getClass(), "NETWORK_SELECTION_KEY");
                        String NETWORK_SELECTION_NAME_KEY = (String) getStaticObjectField(param.getClass(), "NETWORK_SELECTION_NAME_KEY");
                        String NETWORK_SELECTION_SHORT_KEY = (String) getStaticObjectField(param.getClass(), "NETWORK_SELECTION_SHORT_KEY");
                        String LOG_TAG = (String) getStaticObjectField(param.getClass(), "LOG_TAG");

                        int subId = (int) callMethod(param.thisObject, "getSubId");
                        if (Build.VERSION.SDK_INT >= 22) {
                            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                                // open the shared preferences editor, and write the value.
                                // nsm.operatorNumeric is "" if we're in automatic.selection.
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(moduleContext);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString(NETWORK_SELECTION_KEY + subId, nsm.operatorNumeric);
                                editor.putString(NETWORK_SELECTION_NAME_KEY + subId, nsm.operatorAlphaLong);
                                editor.putString(NETWORK_SELECTION_SHORT_KEY + subId, nsm.operatorAlphaShort);
                                // commit and log the result.
                                if (!editor.commit()) {
                                    Rlog.e(LOG_TAG, "failed to commit network selection preference");
                                }
                            } else {
                                Rlog.e(LOG_TAG, "Cannot update network selection preference due to invalid subId " +
                                        subId);
                            }
                        }
                        return true;
                    }
                }
        );*/
    }

    private <T> ArrayList<T> readFile(Context context, String file) {
        ArrayList<T> list = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (ArrayList<T>) ois.readObject();

            fis.close();
            ois.close();
        }  catch (FileNotFoundException e) {
            //XposedBridge.log(TAG + ": Xposed could not find the list " + file);
        } catch (SecurityException e) {
            XposedBridge.log(TAG + ": Xposed does not have permission to read list");
        } catch (Exception e) {
            e.printStackTrace();
            XposedBridge.log(TAG + ": Xposed could not load networks");
        }
        /*if (list.isEmpty())
            XposedBridge.log(TAG + ": Listfile " + file + " is empty");*/
        return list;
    }
}