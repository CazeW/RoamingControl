package net.caze.roamingcontrol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.view.View;

import static android.content.Context.MODE_WORLD_READABLE;
import static net.caze.roamingcontrol.MainActivity.sharedPref;

public class DialogSIMSelect extends DialogFragment implements SharedPreferencesValues {
    private static CheckBoxPreference sim1;
    private static CheckBoxPreference sim2;
    private static String type;

    public static DialogSIMSelect newInstance(String type) {
        DialogSIMSelect frag = new DialogSIMSelect();
        Bundle args = new Bundle();
        args.putString("type", type);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        type = getArguments().getString("type");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sim_select, null);

        builder.setView(view);
        builder.setTitle(getString(R.string.dualsim_select));
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences_simselect, false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
                if (type.equals("national")) {
                    sharedPref.edit().putBoolean(KEY_PREF_NATIONAL_SIM1, sim1.isChecked()).apply();
                    sharedPref.edit().putBoolean(KEY_PREF_NATIONAL_SIM2, sim2.isChecked()).apply();
                }
                else if (type.equals("force")) {
                    sharedPref.edit().putBoolean(KEY_PREF_FORCE_SIM1, sim1.isChecked()).apply();
                    sharedPref.edit().putBoolean(KEY_PREF_FORCE_SIM2, sim2.isChecked()).apply();
                }
            }
        });
        return builder.create();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Fragment frag = getFragmentManager().findFragmentById(R.id.sim_select_fragment);
        getFragmentManager().beginTransaction().remove(frag).commit();
    }

    public static class SIMSelectFragment extends PreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {
            android.support.v7.preference.PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(PREFNAME);
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences_simselect);

            sim1 = (CheckBoxPreference) findPreference("user_sim1");
            sim2 = (CheckBoxPreference) findPreference("user_sim2");
            if (type.equals("national")) {
                if (sharedPref.getBoolean(KEY_PREF_FORCE_SIM1, false))
                    sim1.setEnabled(false);
                sim1.setChecked(sharedPref.getBoolean(KEY_PREF_NATIONAL_SIM1, false));

                if (sharedPref.getBoolean(KEY_PREF_FORCE_SIM2, false))
                    sim2.setEnabled(false);
                sim2.setChecked(sharedPref.getBoolean(KEY_PREF_NATIONAL_SIM2, false));
            }
            else if (type.equals("force")) {
                if (sharedPref.getBoolean(KEY_PREF_NATIONAL_SIM1, false))
                    sim1.setEnabled(false);
                sim1.setChecked(sharedPref.getBoolean(KEY_PREF_FORCE_SIM1, false));

                if (sharedPref.getBoolean(KEY_PREF_NATIONAL_SIM2, false))
                    sim2.setEnabled(false);
                sim2.setChecked(sharedPref.getBoolean(KEY_PREF_FORCE_SIM2, false));
            }
        }
    }
}
