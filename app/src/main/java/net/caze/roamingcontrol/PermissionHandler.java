package net.caze.roamingcontrol;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;


class PermissionHandler implements SharedPreferencesValues {

    static boolean isStoragePermissionGranted(Activity act, String type) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"WRITE_EXTERNAL_STORAGE permission is granted");
                return true;
            } else {
                Log.v(TAG,"WRITE_EXTERNAL_STORAGE permission is revoked");
                if (type.equals("import"))
                    ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                else if (type.equals("export"))
                    ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"WRITE_EXTERNAL_STORAGE permission is granted");
            return true;
        }
    }
    static boolean isPhoneStatePermissionGranted(final Activity act) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(act, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"READ_PHONE_STATE permission is granted");
                return true;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(act, Manifest.permission.READ_PHONE_STATE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setMessage(act.getString(R.string.need_accept_phone_state_permission));
                    builder.setPositiveButton(act.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
                        }
                    });
                    builder.setNegativeButton(act.getString(R.string.cancel), null);
                    builder.show();
                } else {
                    Log.v(TAG, "READ_PHONE_STATE permission is revoked");
                    ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
                }
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"READ_PHONE_STATE permission is granted");
            return true;
        }
    }
}
