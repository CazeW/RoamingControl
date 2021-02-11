package net.caze.roamingcontrol;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class FileOperations implements SharedPreferencesValues {
    private int type;
    private Context context;
    private ListAdapter mAdapter;


    FileOperations(Context context, int type) {
        this(context, null, type);
    }

    FileOperations(Context context, ListAdapter mAdapter, int type) {
        this.context = context;
        this.mAdapter = mAdapter;
        this.type = type;
    }

    <T> ArrayList<T> readFile(Context context, int SIM) {
        ArrayList<T> list = new ArrayList<>();
        try {
            FileInputStream fis;
            if (SIM == 1) {
                if (type == NETWORK)
                    fis = context.openFileInput(NETWORK_FILENAME_SIM1);
                else if (type == FORCE)
                    fis = context.openFileInput(FORCE_FILENAME_SIM1);
                else
                    fis = context.openFileInput(COUNTRY_FILENAME_SIM1);

            } else if (SIM == 2) {
                if (type == NETWORK)
                    fis = context.openFileInput(NETWORK_FILENAME_SIM2);
                else if (type == FORCE)
                    fis = context.openFileInput(FORCE_FILENAME_SIM2);
                else
                    fis = context.openFileInput(COUNTRY_FILENAME_SIM2);
            } else {
                if (type == NETWORK)
                    fis = context.openFileInput(NETWORK_FILENAME);
                else if (type == FORCE)
                    fis = context.openFileInput(FORCE_FILENAME);
                else
                    fis = context.openFileInput(COUNTRY_FILENAME);
            }

            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (ArrayList<T>) ois.readObject();

            fis.close();
            ois.close();
        } catch (FileNotFoundException e) {
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    void updateFile(ArrayList list) {
        try {
            FileOutputStream fos;
            File file;
            if (MainActivity.isDualSIM()) {
                if (MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0) == 0) {
                    if (type == NETWORK) {
                        file = new File(context.getFilesDir(), NETWORK_FILENAME_SIM1);
                        //fos = context.openFileOutput(NETWORK_FILENAME_SIM1, Context.MODE_PRIVATE);
                    } else if (type == FORCE) {
                        file = new File(context.getFilesDir(), FORCE_FILENAME_SIM1);
                        //fos = context.openFileOutput(FORCE_FILENAME_SIM1, Context.MODE_PRIVATE);
                    } else {
                        file = new File(context.getFilesDir(), COUNTRY_FILENAME_SIM1);
                        //fos = context.openFileOutput(COUNTRY_FILENAME_SIM1, Context.MODE_PRIVATE);
                    }
                } else {
                    if (type == NETWORK) {
                        file = new File(context.getFilesDir(), NETWORK_FILENAME_SIM2);
                        //fos = context.openFileOutput(NETWORK_FILENAME_SIM2, Context.MODE_PRIVATE);
                    } else if (type == FORCE) {
                        file = new File(context.getFilesDir(), FORCE_FILENAME_SIM2);
                        //fos = context.openFileOutput(FORCE_FILENAME_SIM2, Context.MODE_PRIVATE);
                    } else {
                        file = new File(context.getFilesDir(), COUNTRY_FILENAME_SIM2);
                        //fos = context.openFileOutput(COUNTRY_FILENAME_SIM2, Context.MODE_PRIVATE);
                    }
                }
            } else {
                if (type == NETWORK) {
                    file = new File(context.getFilesDir(), NETWORK_FILENAME);
                    //fos = context.openFileOutput(NETWORK_FILENAME, Context.MODE_PRIVATE);
                } else if (type == FORCE) {
                    file = new File(context.getFilesDir(), FORCE_FILENAME);
                    //fos = context.openFileOutput(FORCE_FILENAME, Context.MODE_PRIVATE);
                } else {
                    file = new File(context.getFilesDir(), COUNTRY_FILENAME);
                    //fos = context.openFileOutput(COUNTRY_FILENAME, Context.MODE_PRIVATE);
                }
            }
            if (!file.exists())
                file.createNewFile();
            file.setReadable(true, false);
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(list);

            fos.close();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void exportFile() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            String timeStamp = new SimpleDateFormat("_yyyyMMdd-HHmmss").format(new Date());
            File path = new File(Environment.getExternalStorageDirectory(), TAG);
            try {
                if (!path.exists()) {
                    if (!path.mkdir()) {
                        Toast.makeText(context, context.getString(R.string.not_create_folder), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                File file;
                FileInputStream fis;
                if (MainActivity.isDualSIM()) {
                    if (MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0) == 0) {
                        if (type == NETWORK) {
                            file = new File(path, NETWORK_FILENAME_SIM1 + timeStamp);
                            fis = context.openFileInput(NETWORK_FILENAME_SIM1);
                        } else if (type == FORCE) {
                            file = new File(path, FORCE_FILENAME_SIM1 + timeStamp);
                            fis = context.openFileInput(FORCE_FILENAME_SIM1);
                        } else {
                            file = new File(path, COUNTRY_FILENAME_SIM1 + timeStamp);
                            fis = context.openFileInput(COUNTRY_FILENAME_SIM1);
                        }
                    } else {
                        if (type == NETWORK) {
                            file = new File(path, NETWORK_FILENAME_SIM2 + timeStamp);
                            fis = context.openFileInput(NETWORK_FILENAME_SIM2);
                        } else if (type == FORCE) {
                            file = new File(path, FORCE_FILENAME_SIM2 + timeStamp);
                            fis = context.openFileInput(FORCE_FILENAME_SIM2);
                        } else {
                            file = new File(path, COUNTRY_FILENAME_SIM2 + timeStamp);
                            fis = context.openFileInput(COUNTRY_FILENAME_SIM2);
                        }
                    }
                } else {
                    if (type == NETWORK) {
                        file = new File(path, NETWORK_FILENAME + timeStamp);
                        fis = context.openFileInput(NETWORK_FILENAME);
                    } else if (type == FORCE) {
                        file = new File(path, FORCE_FILENAME + timeStamp);
                        fis = context.openFileInput(FORCE_FILENAME);
                    } else {
                        file = new File(path, COUNTRY_FILENAME + timeStamp);
                        fis = context.openFileInput(COUNTRY_FILENAME);
                    }
                }
                FileOutputStream fos = new FileOutputStream(file);
                byte[] data = new byte[fis.available()];
                fis.read(data);
                fos.write(data);
                fis.close();
                fos.close();
                Toast.makeText(context, context.getString(R.string.filed_saved) + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.not_write), Toast.LENGTH_SHORT).show();
        }
    }

    void importFile() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File path = new File(Environment.getExternalStorageDirectory(), TAG);
            DialogSelectFile fileselect = new DialogSelectFile(context, path);
            fileselect.addFileListener(new DialogSelectFile.FileSelectedListener() {
                public void fileSelected(final File file) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        final ArrayList list;
                        if (type == NETWORK || type == FORCE) {
                            list = (ArrayList) ois.readObject();
                            if (!list.isEmpty()) {
                                Network test = (Network) list.get(0);
                            }
                        } else {
                            list = (ArrayList) ois.readObject();
                            if (!list.isEmpty()) {
                                Country test = (Country) list.get(0);
                            }
                        }
                        fis.close();
                        ois.close();

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(context.getString(R.string.replace_list));
                        builder.setMessage(context.getString(R.string.replace_list_message) + " \"" + file.getName() + "\"?");
                        builder.setNegativeButton(context.getString(R.string.no), null);
                        builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                updateFile(list);
                                mAdapter.updateList(list);
                            }
                        });
                        builder.show();
                    } catch (Exception e) {
                        if (type == COUNTRY)
                            Toast.makeText(context, file.getName() + " " + context.getString(R.string.not_valid_country_file), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(context, file.getName() + " " + context.getString(R.string.not_valid_network_file), Toast.LENGTH_LONG).show();
                    }
                }
            });
            fileselect.showDialog();
        } else {
            Toast.makeText(context, context.getString(R.string.not_mount_storage), Toast.LENGTH_SHORT).show();
        }
    }
}
