package net.caze.roamingcontrol;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.preference.CheckBoxPreference;


public class MySettings extends AppCompatActivity implements SharedPreferencesValues {
    private SharedPreferencesManager prefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        prefListener = new SharedPreferencesManager(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.sharedPref.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sharedPref.unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null)
                return;

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFNAME);

            addPreferencesFromResource(R.xml.settings);

            final CheckBoxPreference whitelistMode = (CheckBoxPreference) findPreference(KEY_PREF_WHITELIST_MODE);
            CheckBoxPreference advanced_force = (CheckBoxPreference) findPreference(KEY_PREF_ADVANCED_FORCE);
            advanced_force.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (!whitelistMode.isEnabled())
                        whitelistMode.setChecked(false);
                    return false;
                }
            });
        }
    }
}
