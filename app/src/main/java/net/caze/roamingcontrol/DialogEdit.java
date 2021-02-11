package net.caze.roamingcontrol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DialogEdit extends DialogFragment {

    public static DialogEdit newInstance(int position) {
        DialogEdit frag = new DialogEdit();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int position = getArguments().getInt("position");
        final SavedRoamingNetwork act = ((SavedRoamingNetwork) getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        View view = act.getLayoutInflater().inflate(R.layout.dialog_edit, null);

        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null);
        TextView edit = (TextView) view.findViewById(R.id.edit_button);
        TextView delete = (TextView) view.findViewById(R.id.remove_button);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                DialogFragment addDialog = DialogAddNetwork.newInstance(true, position);
                addDialog.show(getFragmentManager(), getString(R.string.add_network));
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Network network = (Network) act.getmAdapter().getList().get(position);
                Toast.makeText(act, network.getName() + " " + getResources().getString(R.string.removed), Toast.LENGTH_SHORT).show();
                act.getmAdapter().remove(position);
                getDialog().dismiss();
            }
        });
        return builder.create();
    }
}
