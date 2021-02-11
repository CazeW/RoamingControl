package net.caze.roamingcontrol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class DialogHelp extends DialogFragment implements SharedPreferencesValues {

    public static DialogHelp newInstance(int window) {
        DialogHelp frag = new DialogHelp();
        Bundle args = new Bundle();
        args.putInt("window", window);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int window = getArguments().getInt("window");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_help, null);

        builder.setView(view);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(getString(R.string.action_help));

        TextView helpMsg = (TextView) view.findViewById(R.id.helpText);
        /*if (android.os.Build.VERSION.SDK_INT >= 24) {
            if (window == NETWORK)
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_network), Html.FROM_HTML_MODE_LEGACY));
            else if (window == COUNTRY)
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_country), Html.FROM_HTML_MODE_LEGACY));
            else if (window == FORCE)
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_force), Html.FROM_HTML_MODE_LEGACY));
            else
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_main), Html.FROM_HTML_MODE_LEGACY));
        } else {*/
            if (window == NETWORK)
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_network)));
            else if (window == COUNTRY)
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_country)));
            else if (window == FORCE)
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_force)));
            else
                helpMsg.setText(Html.fromHtml(getActivity().getString(R.string.help_message_main)));
        //}

        helpMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setNeutralButton(getActivity().getString(R.string.close), null);

        return builder.create();
    }
}
