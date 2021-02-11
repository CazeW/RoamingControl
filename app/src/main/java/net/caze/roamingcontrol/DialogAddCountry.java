package net.caze.roamingcontrol;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class DialogAddCountry extends DialogFragment {

    public static DialogAddCountry newInstance() {
        return new DialogAddCountry();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NetworkInfo networkInfo = NetworkInfo.getInstance();
        final SavedRoamingCountry act = ((SavedRoamingCountry) getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(getResources().getString(R.string.add_country));
        final String[] list = networkInfo.getCountryList().values().toArray(new String[networkInfo.getCountryList().values().toArray().length]);
        builder.setItems(list, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String mcc = networkInfo.searchKey(list[which]);
                Country country = new Country(list[which], networkInfo.baseMcc(mcc));
                if (!act.getmAdapter().listContains(false, act.getCountryList(), country, null)) {
                        Toast.makeText(act, country.getName() + " " + getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();
                        act.getmAdapter().add(country);
                } else
                    Toast.makeText(act, list[which] + " " + getString(R.string.already_exists_country), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);
        AlertDialog mdialog = builder.create();
        mdialog.setCanceledOnTouchOutside(false);
        return mdialog;
    }
}
