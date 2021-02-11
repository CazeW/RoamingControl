package net.caze.roamingcontrol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DialogAddNetwork extends DialogFragment implements SharedPreferencesValues {

    public static DialogAddNetwork newInstance(boolean editMode, int position) {
        DialogAddNetwork frag = new DialogAddNetwork();
        Bundle args = new Bundle();
        args.putBoolean("editMode", editMode);
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NetworkInfo networkInfo = NetworkInfo.getInstance();
        final SavedRoamingNetwork act = ((SavedRoamingNetwork) getActivity());

        final boolean editMode = getArguments().getBoolean("editMode");
        final int position = getArguments().getInt("position");

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        View view = act.getLayoutInflater().inflate(R.layout.dialog_add, null);
        final EditText name = (EditText) view.findViewById(R.id.manualName);
        final EditText mcc = (EditText) view.findViewById(R.id.manualMCC);
        final EditText mnc = (EditText) view.findViewById(R.id.manualMNC);
        final Network oldnetwork;

        builder.setView(view);
        if (!editMode) {
            builder.setTitle(getResources().getString(R.string.add_network));
            builder.setPositiveButton(getResources().getString(R.string.add), null);
            oldnetwork = null;
        } else {
            builder.setTitle(R.string.edit_network);
            builder.setPositiveButton(getResources().getString(R.string.save), null);
            oldnetwork = (Network) act.getmAdapter().getList().get(position);
            name.setText(oldnetwork.getName());
            mcc.setText(oldnetwork.getMcc());
            mnc.setText(oldnetwork.getMnc());
        }

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getDialog().dismiss();
            }
        });

        final AlertDialog mdialog = builder.create();
        mdialog.setCanceledOnTouchOutside(false);
        mdialog.show();
        if (!editMode) {
            mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
        mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String mnc_nr = mnc.getText().toString();
                String mcc_nr = mcc.getText().toString();
                String operator_country = networkInfo.getOperatorCountry(mcc_nr);
                String operator_name = name.getText().toString();

                Network network = new Network(operator_name, operator_country, mcc_nr, mnc_nr);
                Boolean listContains = false;
                /*if (MainActivity.sharedPref.getBoolean(KEY_PREF_MATCH_NAME, false)) {
                    if (act.getmAdapter().listContains(true, act.getNetworkList(), network, oldnetwork)) {
                        listContains = true;
                        Toast.makeText(act, getResources().getString(R.string.already_exists_namemcc), Toast.LENGTH_SHORT).show();
                    }
                } else {*/
                    if (act.getmAdapter().listContains(false, act.getNetworkList(), network, oldnetwork)) {
                        listContains = true;
                        Toast.makeText(act, getResources().getString(R.string.already_exists_mccmnc), Toast.LENGTH_SHORT).show();
                    }
                //}
                if (!listContains) {
                    if (!editMode) {
                        Toast.makeText(act, network.getName() + " " + getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                        act.getmAdapter().add(network);
                        getDialog().dismiss();
                    } else {
                        act.getmAdapter().edit(position, network);
                        getDialog().dismiss();
                    }
                }
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String operator_country = networkInfo.getOperatorCountry(mcc.getText().toString());
                int mnclength = mnc.getText().length();
                if (TextUtils.isEmpty(s) || operator_country.equals(getResources().getString(R.string.unknown)) || mnclength < 2)
                    mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });
        mcc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String operator_country = networkInfo.getOperatorCountry(mcc.getText().toString());
                int mnclength = mnc.getText().length();
                if (operator_country.equals(getResources().getString(R.string.unknown))) {
                    mcc.setTextColor(Color.RED);
                    mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    mcc.setTextColor(Color.BLACK);
                    if (!name.getText().toString().isEmpty() && mnclength >= 2)
                        mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        mnc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String operator_country = networkInfo.getOperatorCountry(mcc.getText().toString());
                int mnclength = mnc.getText().length();
                if (mnclength < 2) {
                    mnc.setTextColor(Color.RED);
                    mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    mnc.setTextColor(Color.BLACK);
                    if (!name.getText().toString().isEmpty() && !operator_country.equals(getResources().getString(R.string.unknown)))
                    mdialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        return mdialog;
    }
}
