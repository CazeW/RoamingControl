package net.caze.roamingcontrol;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;

import static net.caze.roamingcontrol.MainActivity.pref_saved;
import static net.caze.roamingcontrol.MainActivity.pref_user_forceroaming;
import static net.caze.roamingcontrol.MainActivity.pref_user_national;
import static net.caze.roamingcontrol.MainActivity.roaming_type;
import static net.caze.roamingcontrol.MainActivity.styleText;


class SharedPreferencesManager implements SharedPreferences.OnSharedPreferenceChangeListener, SharedPreferencesValues {
    private TextView managesaved_title, managesaved_sum;
    private Activity activity;
    private ComponentName componentName;

    SharedPreferencesManager(Activity activity) {
        this.activity = activity;
        componentName = new ComponentName(activity, "net.caze.roamingcontrol.Activity-Alias");
    }

    SharedPreferencesManager(TextView title, TextView sum, Activity activity) {
        this.managesaved_title = title;
        this.managesaved_sum = sum;
        this.activity = activity;
        componentName = new ComponentName(activity, "net.caze.roamingcontrol.Activity-Alias");
        if (MainActivity.sharedPref.getString(KEY_PREF_ROAMING_TYPE, NETWORK_VALUE).equals(COUNTRY_VALUE)) {
            this.managesaved_title.setText(activity.getResources().getString(R.string.manage_countries));
            this.managesaved_sum.setText(activity.getResources().getString(R.string.manage_countries));
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

        String natSum = pref_user_national.getSummary().toString();
        Spannable natSpan = new SpannableString(pref_user_national.getSummary());

        String forceSum = pref_user_forceroaming.getSummary().toString();
        Spannable forceSpan = new SpannableString(pref_user_forceroaming.getSummary());

        String sim1 = "(" + activity.getResources().getString(R.string.sim1) + ")";
        String sim2 = "(" + activity.getResources().getString(R.string.sim2) + ")";

        switch (key) {
            case KEY_PREF_NATIONAL:
                if (MainActivity.isDualSIM()) {
                    if (pref.getBoolean(KEY_PREF_NATIONAL_SIM1, false) || pref.getBoolean(KEY_PREF_NATIONAL_SIM2, false)) {
                        pref_user_national.setChecked(true);
                    } else {
                        pref_user_national.setChecked(false);
                    }
                }
                else if (pref.getBoolean(key, false) && !pref.getBoolean(KEY_PREF_ADVANCED_FORCE, false)) {
                    pref_user_forceroaming.setChecked(false);
                    pref_user_forceroaming.setEnabled(false);
                }
                else {
                    pref_user_forceroaming.setEnabled(true);
                }
                break;
            case KEY_PREF_NATIONAL_SIM1:
                if (pref.getBoolean(key, false)) {
                    natSpan = styleText(natSpan, natSum.indexOf(sim1), natSum.indexOf(sim1) + sim1.length(), ContextCompat.getColor(activity, R.color.colorPrimary), true);
                    pref_user_national.setChecked(true);
                } else {
                    natSpan = styleText(natSpan, natSum.indexOf(sim1), natSum.indexOf(sim1) + sim1.length(), Color.GRAY, false);
                    pref_user_national.setChecked(false);
                }
                pref_user_national.setSummary(natSpan);
                break;
            case KEY_PREF_NATIONAL_SIM2:
                if (pref.getBoolean(key, false)) {
                    natSpan = styleText(natSpan, natSum.indexOf(sim2), natSum.indexOf(sim2) + sim2.length(), ContextCompat.getColor(activity, R.color.colorPrimary), true);
                    pref_user_national.setChecked(true);
                } else {
                    natSpan = styleText(natSpan, natSum.indexOf(sim2), natSum.indexOf(sim2) + sim2.length(), Color.GRAY, false);
                    pref_user_national.setChecked(false);
                }
                pref_user_national.setSummary(natSpan);
                break;

            case KEY_PREF_FORCE:
                if (MainActivity.isDualSIM() && !pref.getBoolean(KEY_PREF_ADVANCED_FORCE, false)) {
                    if (pref.getBoolean(KEY_PREF_FORCE_SIM1, false) || pref.getBoolean(KEY_PREF_FORCE_SIM2, false)) {
                        pref_user_forceroaming.setChecked(true);
                    } else {
                        pref_user_forceroaming.setChecked(false);
                    }
                }
                else if (pref.getBoolean(key, false) && !pref.getBoolean(KEY_PREF_ADVANCED_FORCE, false)) {
                    pref_user_national.setChecked(false);
                    pref_user_national.setEnabled(false);
                }
                else {
                    pref_user_national.setEnabled(true);
                }
                break;
            case KEY_PREF_FORCE_SIM1:
                if (pref.getBoolean(key, false)) {
                    forceSpan = styleText(forceSpan, forceSum.indexOf(sim1), forceSum.indexOf(sim1) + sim1.length(), ContextCompat.getColor(activity, R.color.colorPrimary), true);
                    pref_user_forceroaming.setChecked(true);
                } else {
                    forceSpan = styleText(forceSpan, forceSum.indexOf(sim1), forceSum.indexOf(sim1) + sim1.length(), Color.GRAY, false);
                    pref_user_forceroaming.setChecked(false);
                }
                pref_user_forceroaming.setSummary(forceSpan);
                break;
            case KEY_PREF_FORCE_SIM2:
                if (pref.getBoolean(key, false)) {
                    forceSpan = styleText(forceSpan, forceSum.indexOf(sim2), forceSum.indexOf(sim2) + sim2.length(), ContextCompat.getColor(activity, R.color.colorPrimary), true);
                    pref_user_forceroaming.setChecked(true);
                } else {
                    forceSpan = styleText(forceSpan, forceSum.indexOf(sim2), forceSum.indexOf(sim2) + sim2.length(), Color.GRAY, false);
                    pref_user_forceroaming.setChecked(false);
                }
                pref_user_forceroaming.setSummary(forceSpan);
                break;

            case KEY_PREF_WHITELIST_MODE:
                if (pref.getBoolean(key, false))
                    pref_user_forceroaming.setSummary(activity.getString(R.string.whitelist_mode));
                else
                    pref_user_forceroaming.setSummary(activity.getString(R.string.force_roaming_summ));
                break;

            case KEY_PREF_ADVANCED_FORCE:
                pref_user_forceroaming.setChecked(false);
                if (pref.getBoolean(key, false)) {
                    pref_user_forceroaming.setEnabled(true);
                    if (MainActivity.isDualSIM()) {
                        pref.edit().putBoolean(KEY_PREF_FORCE_SIM1, false).apply();
                        pref.edit().putBoolean(KEY_PREF_FORCE_SIM2, false).apply();
                        pref_user_forceroaming.setSummary(activity.getString(R.string.force_roaming_summ));
                        pref_user_forceroaming.setOnPreferenceClickListener(null);
                    }
                }
                if (!pref.getBoolean(key, false) && pref.getBoolean(KEY_PREF_NATIONAL, false) && !MainActivity.isDualSIM()) {
                    pref_user_forceroaming.setEnabled(false);
                }
                break;

            case KEY_PREF_ROAMING_TYPE:
                try {
                    if (roaming_type.getValue().equals(COUNTRY_VALUE)) {
                        pref_saved.setTitle(activity.getResources().getString(R.string.saved_roaming_country));
                        pref_saved.setSummary(activity.getResources().getString(R.string.saved_roaming_country_summ));
                        managesaved_title.setText(activity.getResources().getString(R.string.manage_countries));
                        managesaved_sum.setText(activity.getResources().getString(R.string.manage_countries));
                        //pref_manage.setIntent(new Intent(getApplicationContext(), Countries.class));
                    } else {
                        pref_saved.setTitle(activity.getResources().getString(R.string.saved_roaming_network));
                        pref_saved.setSummary(activity.getResources().getString(R.string.saved_roaming_network_summ));
                        managesaved_title.setText(activity.getResources().getString(R.string.manage_networks));
                        managesaved_sum.setText(activity.getResources().getString(R.string.manage_networks));
                        /*if (isDualSIM()) {
                            advancedforceItem.setChecked(true);
                            advancedForce = true;
                            pref_manage.setIntent(new Intent(getApplicationContext(), Networks_DualSIM.class));
                        } else {
                            advancedforceItem.setEnabled(true);
                            pref_user_national.setEnabled(true);
                            pref_manage.setIntent(new Intent(getApplicationContext(), Networks.class));
                        }*/
                    }
                    //editor.putBoolean("user_advanced_force", advancedForce);
                    //editor.commit();
                    if (roaming_type.getValue().equals(COUNTRY_VALUE))
                        roaming_type.setSummary(activity.getResources().getString(R.string.country) + " " + activity.getResources().getString(R.string.mode));
                    else
                        roaming_type.setSummary(activity.getResources().getString(R.string.network) + " " + activity.getResources().getString(R.string.mode));
                } catch (NullPointerException ignore) {}
                break;

            case KEY_PREF_HIDE_ICON:
                if (pref.getBoolean(key, false)) {
                    activity.getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    activity.getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
                break;
        }
    }
}
