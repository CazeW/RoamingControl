package net.caze.roamingcontrol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogAbout extends DialogFragment implements SharedPreferencesValues {

    public static DialogAbout newInstance() {
        return new DialogAbout();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);

        builder.setView(view);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(getActivity().getString(R.string.app_name) + " " + getActivity().getString(R.string.version));

        TextView aboutMsg = (TextView) view.findViewById(R.id.aboutText);
        /*if (android.os.Build.VERSION.SDK_INT >= 24) {
            aboutMsg.setText(Html.fromHtml(getActivity().getString(R.string.about_message), Html.FROM_HTML_MODE_LEGACY));
        } else {*/
            aboutMsg.setText(Html.fromHtml(getActivity().getString(R.string.about_message)));
        //}
        aboutMsg.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageButton donate = (ImageButton) view.findViewById(R.id.paypal_button);
        donate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/cazew"));
                startActivity(browserIntent);
            }
        });
        builder.setNeutralButton(getActivity().getString(R.string.close), null);

        return builder.create();
    }
}
