package net.caze.roamingcontrol;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.telephony.SubscriptionManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SharedPreferencesValues {

    protected static SwitchPreference pref_user_national, pref_user_forceroaming;
    protected static Preference pref_saved;
    protected static ListPreference roaming_type;
    private CardView forcedroaming;

    static SharedPreferences sharedPref;
    private SharedPreferencesManager sharedListener;

    private static boolean dualSIM = false; //Flag for if the phone is dualsim

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(PREFNAME, MODE_WORLD_READABLE);

        PreferenceManager.setDefaultValues(this, R.xml.preferences_national, false);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_saved, false);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_other, false);

        //Migrate old settings
        BackCompHandler.migrateSettings(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NetworkInfo.getInstance(getApplication());
        forcedroaming = (CardView) findViewById(R.id.forcedroaming);
        TextView managesaved_title = (TextView) findViewById(R.id.saved);
        TextView managesaved_sum = (TextView) findViewById(R.id.savedsum);

        //Perform checks
        checkRoamingSetting();
        checkDualsim();

        if (isDualSIM()) {
            setDualSIMPrefs("national");
        }

        sharedListener = new SharedPreferencesManager(managesaved_title, managesaved_sum, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPref.getBoolean(KEY_PREF_ADVANCED_FORCE, false)) {
            if (forcedroaming.getVisibility() != View.VISIBLE) {
                forcedroaming.setVisibility(View.VISIBLE);
            }
        } else {
            if (forcedroaming.getVisibility() != View.GONE) {
                forcedroaming.setVisibility(View.GONE);
            }
            if (isDualSIM()) {
                setDualSIMPrefs("force");
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(sharedListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(sharedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, MySettings.class);
            startActivity(intent);
        } else if (id == R.id.action_help) {
            DialogFragment helpDialog = DialogHelp.newInstance(0);
            helpDialog.show(getFragmentManager(), getString(R.string.action_help));
        } else {
            DialogFragment aboutDialog = DialogAbout.newInstance();
            aboutDialog.show(getFragmentManager(), getString(R.string.action_about));
        }
        return super.onOptionsItemSelected(item);
    }


    public void openForced(View view) {
        Intent intent = new Intent(this, SavedRoamingNetwork.class);
        intent.putExtra("FORCE_OR_NETWORK", "FORCE");
        startActivity(intent);
    }
    public void openNetworks(View view) {
        if (sharedPref.getString(KEY_PREF_ROAMING_TYPE, NETWORK_VALUE).equals(NETWORK_VALUE)) {
            Intent intent = new Intent(this, SavedRoamingNetwork.class);
            intent.putExtra("FORCE_OR_NETWORK", "NETWORK");
            startActivity(intent);
        } else
            startActivity(new Intent(this, SavedRoamingCountry.class));
    }


    public static class NationalFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {
            android.support.v7.preference.PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFNAME);
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences_national);

            pref_user_national = (SwitchPreference) findPreference(KEY_PREF_NATIONAL);
            pref_user_forceroaming = (SwitchPreference) findPreference(KEY_PREF_FORCE);
            if (sharedPref.getBoolean(KEY_PREF_WHITELIST_MODE, false))
                pref_user_forceroaming.setSummary(getString(R.string.whitelist_mode));
        }
    }

    public static class SavedFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {
            android.support.v7.preference.PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFNAME);
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences_saved);

            pref_saved = findPreference(KEY_PREF_SAVED_ROAMING);
            roaming_type = (ListPreference) findPreference(KEY_PREF_ROAMING_TYPE);

            if (roaming_type.getValue().equals(COUNTRY_VALUE)) {
                pref_saved.setTitle(getResources().getString(R.string.saved_roaming_country));
                pref_saved.setSummary(getResources().getString(R.string.saved_roaming_country_summ));
                roaming_type.setSummary(getResources().getString(R.string.country) + " " + getResources().getString(R.string.mode));
            } else {
                roaming_type.setSummary(getResources().getString(R.string.network) + " " + getResources().getString(R.string.mode));
            }
        }
    }

    public static class OtherFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {
            android.support.v7.preference.PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFNAME);
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences_other);

        }
    }

    private void checkRoamingSetting() {
        boolean enabled;
        if (Build.VERSION.SDK_INT >= 17)
            enabled = Settings.Global.getInt(getContentResolver(), Settings.Global.DATA_ROAMING, 0) == 1;
        else
            enabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.DATA_ROAMING, 0) == 1;

        if (enabled && !sharedPref.getBoolean(DIALOG_ROAMING_SKIP, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View content = getLayoutInflater().inflate(R.layout.dialog_roaming_setting_check, null);
            builder.setView(content);
            builder.setTitle(getString(R.string.roaming_setting));
            builder.setMessage(getString(R.string.roaming_setting_message));
            CheckBox skip = (CheckBox) content.findViewById(R.id.dialog_roaming_skip);
            skip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sharedPref.edit().putBoolean(DIALOG_ROAMING_SKIP, isChecked).apply();
                }
            });
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int which) {
                    startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                }
            });
            builder.setNegativeButton(getString(R.string.notnow), null);
            builder.setCancelable(false);
            builder.show();
        }
    }

    private void checkDualsim() {
        if (Build.VERSION.SDK_INT >= 22) {
            //TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            SubscriptionManager subMan = SubscriptionManager.from(this);
            //if (tel.getSimCount() > 1)
            if (subMan.getActiveSubscriptionInfoCountMax() > 1)
                dualSIM = true;
        }
        else {
            if (!getSystemProperty("gsm.sim.operator.numeric.2").equals("")) {
                dualSIM = true;
            }
        }
        //dualSIM = true;

        if (dualSIM) {
            PermissionHandler.isPhoneStatePermissionGranted(this);
            sharedPref.edit().putBoolean(KEY_PREF_DUALSIM, true).apply();
        }
        else {
            sharedPref.edit().putBoolean(KEY_PREF_DUALSIM, false).apply();
        }
    }

    private void setDualSIMPrefs(String pref) {
        String enabled = getString(R.string.enabled) + ": ";
        String sim1 = "(" + getString(R.string.sim1) +") ";
        String sim2 = "(" + getString(R.string.sim2) +")";

        if (pref.equals("national")) {
            String natSum = getString(R.string.national_roaming_summ) + "\n" + enabled + sim1 + sim2;
            Spannable natSpan = new SpannableString(natSum);
            natSpan = styleText(natSpan, natSum.indexOf(enabled), natSum.indexOf(enabled) + enabled.length(), ContextCompat.getColor(this, R.color.colorPrimary), true);

            if (sharedPref.getBoolean(KEY_PREF_NATIONAL_SIM1, false)) {
                natSpan = styleText(natSpan, natSum.indexOf(sim1), natSum.indexOf(sim1) + sim1.length(), ContextCompat.getColor(this, R.color.colorPrimary), true);
            } else {
                natSpan = styleText(natSpan, natSum.indexOf(sim1), natSum.indexOf(sim1) + sim1.length(), Color.GRAY, false);
            }
            if (sharedPref.getBoolean(KEY_PREF_NATIONAL_SIM2, false)) {
                natSpan = styleText(natSpan, natSum.indexOf(sim2), natSum.indexOf(sim2) + sim2.length(), ContextCompat.getColor(this, R.color.colorPrimary), true);
            } else {
                natSpan = styleText(natSpan, natSum.indexOf(sim2), natSum.indexOf(sim2) + sim2.length(), Color.GRAY, false);
            }
            pref_user_national.setSummary(natSpan);
            pref_user_national.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogFragment dialogSIMSelect = DialogSIMSelect.newInstance("national");
                    dialogSIMSelect.show(getFragmentManager(), getString(R.string.dualsim_select));
                    return true;
                }
            });

        } else if (pref.equals("force")){
            String forceSum;
            if (sharedPref.getBoolean(KEY_PREF_WHITELIST_MODE, false))
                forceSum = getString(R.string.whitelist_mode) + "\n" + enabled + sim1 + sim2;
            else
                forceSum = getString(R.string.force_roaming_summ) + "\n" + enabled + sim1 + sim2;
            Spannable forceSpan = new SpannableString(forceSum);
            forceSpan = styleText(forceSpan, forceSum.indexOf(enabled), forceSum.indexOf(enabled) + enabled.length(), ContextCompat.getColor(this, R.color.colorPrimary), true);

            if (sharedPref.getBoolean(KEY_PREF_FORCE_SIM1, false)) {
                forceSpan = styleText(forceSpan, forceSum.indexOf(sim1), forceSum.indexOf(sim1) + sim1.length(), ContextCompat.getColor(this, R.color.colorPrimary), true);
            } else {
                forceSpan = styleText(forceSpan, forceSum.indexOf(sim1), forceSum.indexOf(sim1) + sim1.length(), Color.GRAY, false);
            }
            if (sharedPref.getBoolean(KEY_PREF_FORCE_SIM2, false)) {
                forceSpan = styleText(forceSpan, forceSum.indexOf(sim2), forceSum.indexOf(sim2) + sim2.length(), ContextCompat.getColor(this, R.color.colorPrimary), true);
            } else {
                forceSpan = styleText(forceSpan, forceSum.indexOf(sim2), forceSum.indexOf(sim2) + sim2.length(), Color.GRAY, false);
            }
            pref_user_forceroaming.setSummary(forceSpan);
            pref_user_forceroaming.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogFragment dialogSIMSelect = DialogSIMSelect.newInstance("force");
                    dialogSIMSelect.show(getFragmentManager(), getString(R.string.dualsim_select));
                    return true;
                }
            });
        }
    }

    static boolean isDualSIM() {
        return dualSIM;
    }

    static Spannable styleText(Spannable text, int start, int end, int color, boolean bold) {
        text.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold)
            text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        else {
            StyleSpan[] ss = text.getSpans(start, end, StyleSpan.class);
            for (StyleSpan s : ss) {
                if (s.getStyle() == Typeface.BOLD) {
                    text.removeSpan(s);
                    break;
                }
            }
        }
        return text;
    }

    private String getSystemProperty(String key) {
        String value = null;
        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
