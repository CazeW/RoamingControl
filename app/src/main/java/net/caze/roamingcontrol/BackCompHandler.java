package net.caze.roamingcontrol;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

class BackCompHandler implements SharedPreferencesValues {

    static void migrateSettings(Context context) {
        if (MainActivity.sharedPref.getBoolean(KEY_PREF_MIGRATIONE_DONE, false))
            return;

        Set<String> saved_networks;
        String file;
        NetworkInfo networkInfo = NetworkInfo.getInstance();

        //Migrate really old settings
        Set<String> saved_networks_xposed = new TreeSet<>(MainActivity.sharedPref.getStringSet(REALLY_OLD_SAVED_NETWORKS_XPOSED, new TreeSet<String>()));
        if (!saved_networks_xposed.isEmpty()) {
            saved_networks = new TreeSet<>();
            for (String snetwork_xposed : saved_networks_xposed) {
                String[] prefText = TextUtils.split(snetwork_xposed, "‚‗‚");
                String prefName = prefText[0];
                String[] prefMCC_MNC = TextUtils.split(prefText[1], "‚_‚");
                String prefCountry = networkInfo.getOperatorCountry(prefMCC_MNC[0]);
                saved_networks.add(prefCountry + "‚‗‚" + prefName + "‚‗‚" + prefMCC_MNC[0] + "‚_‚" + prefMCC_MNC[1]);
            }
            MainActivity.sharedPref.edit().remove(REALLY_OLD_SAVED_NETWORKS_XPOSED).apply();
            MainActivity.sharedPref.edit().putStringSet(OLD_SAVED_NETWORKS, saved_networks).apply();
        }

        //Migrate networks
        for (int i = 1; i <= 6; i++) {
            ArrayList<Network> list = new ArrayList<>();
            if (i == 1) {
                saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_SAVED_NETWORKS, new TreeSet<String>()));
                file = NETWORK_FILENAME;
            } else if (i == 2) {
                saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_FORCE_SAVED_NETWORKS, new TreeSet<String>()));
                file = FORCE_FILENAME;
            } else if (i == 3) {
                saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_SAVED_NETWORKS_SIM1, new TreeSet<String>()));
                file = NETWORK_FILENAME_SIM1;
            } else if (i == 4) {
                saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_SAVED_NETWORKS_SIM2, new TreeSet<String>()));
                file = NETWORK_FILENAME_SIM2;
            } else if (i == 5) {
                saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_FORCE_SAVED_NETWORKS_SIM1, new TreeSet<String>()));
                file = FORCE_FILENAME_SIM1;
            } else {
                saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_FORCE_SAVED_NETWORKS_SIM2, new TreeSet<String>()));
                file = FORCE_FILENAME_SIM2;
            }
            if (!saved_networks.isEmpty()) {
                Network newNetwork;
                String[] oldNetwork;
                for (String network : saved_networks) {
                    oldNetwork = TextUtils.split(network, "‚‗‚");
                    String[] oldMccMnc = TextUtils.split(oldNetwork[2], "‚_‚");
                    newNetwork = new Network(oldNetwork[1], oldNetwork[0], oldMccMnc[0], oldMccMnc[1]);
                    list.add(newNetwork);
                }
                if (!list.isEmpty()) {
                    if (fileWriter(context, list, file)) {
                        if (i == 1)
                            MainActivity.sharedPref.edit().remove(OLD_SAVED_NETWORKS).apply();
                        else if (i == 2)
                            MainActivity.sharedPref.edit().remove(OLD_FORCE_SAVED_NETWORKS).apply();
                        else if (i == 3)
                            MainActivity.sharedPref.edit().remove(OLD_SAVED_NETWORKS_SIM1).apply();
                        else if (i == 4)
                            MainActivity.sharedPref.edit().remove(OLD_SAVED_NETWORKS_SIM2).apply();
                        else if (i == 5)
                            MainActivity.sharedPref.edit().remove(OLD_FORCE_SAVED_NETWORKS_SIM1).apply();
                        else
                            MainActivity.sharedPref.edit().remove(OLD_FORCE_SAVED_NETWORKS_SIM2).apply();
                    } else {
                        Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        //Migrate countries
        ArrayList<Country> list = new ArrayList<>();
        saved_networks = new TreeSet<>(MainActivity.sharedPref.getStringSet(OLD_SAVED_COUNTRIES, new TreeSet<String>()));
        file = COUNTRY_FILENAME;
        if (!saved_networks.isEmpty()) {
            Country newCountry;
            String[] oldCountry;
            for (String country : saved_networks) {
                oldCountry = TextUtils.split(country, "‚‗‚");
                newCountry = new Country(oldCountry[0], oldCountry[1]);
                list.add(newCountry);
            }
            if (!list.isEmpty()) {
                if (fileWriter(context, list, file))
                    MainActivity.sharedPref.edit().remove(OLD_SAVED_COUNTRIES).apply();
                else {
                    Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        //Migrate whitelist setting
        String type = MainActivity.sharedPref.getString(KEY_PREF_ROAMING_TYPE, NETWORK_VALUE);
        if (type.equals(context.getString(R.string.whitelist))) {
            MainActivity.sharedPref.edit().putString(KEY_PREF_ROAMING_TYPE, NETWORK_VALUE).apply();
            MainActivity.sharedPref.edit().putBoolean(KEY_PREF_WHITELIST_MODE, true).apply();
            MainActivity.sharedPref.edit().putBoolean(KEY_PREF_FORCE, true).apply();
        }

        //Set name matching
        if (MainActivity.sharedPref.getBoolean(KEY_PREF_NATIONAL, false) ||
                MainActivity.sharedPref.getBoolean(KEY_PREF_FORCE, false) ||
                MainActivity.sharedPref.getBoolean(KEY_PREF_SAVED_ROAMING, false) ||
                MainActivity.sharedPref.getBoolean(KEY_PREF_HIDE_ICON, false)) {
            MainActivity.sharedPref.edit().putBoolean(KEY_PREF_MATCH_NAME, true).apply();
        }

        //Remove old settings
        MainActivity.sharedPref.edit().remove(OLD_SHOW_MCC).apply();
        MainActivity.sharedPref.edit().remove(OLD_SHOW_MNC).apply();
        MainActivity.sharedPref.edit().remove(OLD_DUALSIM_POPUP).apply();


        //Migration done
        Log.v(TAG, context.getString(R.string.migration_success));
        MainActivity.sharedPref.edit().putBoolean(KEY_PREF_MIGRATIONE_DONE, true).apply();
    }


    private static boolean fileWriter(Context context, ArrayList list, String file) {
        try {
            FileOutputStream fos = context.openFileOutput(file, Context.MODE_WORLD_READABLE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);
            fos.close();
            oos.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
