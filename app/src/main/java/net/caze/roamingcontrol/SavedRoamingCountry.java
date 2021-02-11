package net.caze.roamingcontrol;


import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class SavedRoamingCountry extends AppCompatActivity implements ListAdapter.MyViewHolder.ClickListener, SharedPreferencesValues {
    private ArrayList<Country> countryList;
    private NetworkInfo networkInfo;
    private ListAdapter mAdapter;
    private Country currentCountry;
    private RecyclerView mRecyclerView;
    private Button simselect;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_savedroaming_country);

        //For dualSIM
        simselect = (Button) findViewById(R.id.select_sim);
        if (!MainActivity.isDualSIM()) {
            simselect.setVisibility(View.GONE);
        } else {
            MainActivity.sharedPref.edit().putInt(KEY_PREF_SIM_SELECTED, 0).apply();
        }

        mAdapter = new ListAdapter(this, COUNTRY);
        countryList = mAdapter.getList();

        mRecyclerView = (RecyclerView) findViewById(R.id.countrylist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        networkInfo = NetworkInfo.getInstance();
        displayOperatorInfo();
    }

    public void simSelect(View view) {
        if (actionMode != null) {
            actionMode.finish();
        }
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.popup_simselect);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.sim1 && MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0) != 0) {
                    MainActivity.sharedPref.edit().putInt(KEY_PREF_SIM_SELECTED, 0).apply();
                    displayOperatorInfo();
                    simselect.setText(getString(R.string.sim1));
                    mAdapter.switchSIMList(0);
                    countryList = mAdapter.getList();
                } else if (id == R.id.sim2 && MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0) != 1) {
                    MainActivity.sharedPref.edit().putInt(KEY_PREF_SIM_SELECTED, 1).apply();
                    displayOperatorInfo();
                    simselect.setText(getString(R.string.sim2));
                    mAdapter.switchSIMList(1);
                    countryList = mAdapter.getList();
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_saved_country, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_import:
                if (PermissionHandler.isStoragePermissionGranted(this, "import")) {
                    mAdapter.importList();
                }
                return true;
            case R.id.action_export:
                if (PermissionHandler.isStoragePermissionGranted(this, "export"))
                    mAdapter.exportList();
                return true;
            case R.id.action_help:
                DialogFragment helpDialog = DialogHelp.newInstance(COUNTRY);
                helpDialog.show(getFragmentManager(), getString(R.string.action_help));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(final int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {
            final Country selected = (Country) mAdapter.getList().get(position);
            AlertDialog.Builder removeDialog = new AlertDialog.Builder(this);
            removeDialog.setTitle(getString(R.string.remove) + " \"" + selected.getName() + "\"?");
            removeDialog.setMessage(getString(R.string.remove_dialog_country));
            removeDialog.setNegativeButton(getString(R.string.no), null);
            removeDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplication(), selected.getName() + " " + getString(R.string.removed), Toast.LENGTH_SHORT).show();
                    mAdapter.remove(position);
                }
            });
            removeDialog.show();
        }
    }
    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);

        return true;
    }
    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count) + " " + getString(R.string.selected));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        FloatingActionButton fab_add;
        FloatingActionButton fab_delete;
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            fab_add = (FloatingActionButton) findViewById(R.id.add_button);
            fab_delete = (FloatingActionButton) findViewById(R.id.delete_button);
            fab_add.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    fab_delete.show();
                }
            });
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fab_delete.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    fab_add.show();
                }
            });
            mAdapter.clearSelection();
            actionMode = null;
        }
    }


    private void displayOperatorInfo() {
        TextView currentName = (TextView) findViewById(R.id.currentname);
        TextView currentMcc = (TextView) findViewById(R.id.currentmcc);

        Network network;
        /*if (MainActivity.isDualSIM() && !PermissionHandler.isPhoneStatePermissionGranted(this))
            network = new Network(getString(R.string.unknown), "", "---", "---");
        else
            network = networkInfo.getCurrentNetwork(this);*/

        network = networkInfo.getCurrentNetwork(this);
        currentCountry = new Country(network.getCountry(), network.getMcc());

        if (currentCountry.getName().equals("")) {
            currentName.setText(getString(R.string.unknown));
            currentMcc.setText(currentCountry.getMcc_text());
        } else {
            currentName.setText(currentCountry.getName());
            currentMcc.setText(currentCountry.getMcc_text());
        }

        TextView siminfo = (TextView) findViewById(R.id.sim_info);

        if (MainActivity.isDualSIM()) {
            String simInfo = networkInfo.getSimNetwork(this);
            if (!simInfo.isEmpty())
                siminfo.setText("MCC: " + simInfo.substring(0, 3) + " (MNC: " + simInfo.substring(3) + ")");
            else
                siminfo.setText("MCC: --- (MNC: ---)");
        } else {
            siminfo.setVisibility(View.GONE);
        }
    }

    public void addCurrentCountry(View view) {
        if (!mAdapter.listContains(false, countryList, currentCountry, null)) {
            if (currentCountry.getMcc().equals("---")) {
                Toast.makeText(this, getString(R.string.unknown_network), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.current_country_added), Toast.LENGTH_SHORT).show();
                mAdapter.add(currentCountry);
            }
        }
    }

    public void addNewCountry(View view) {
        DialogFragment addDialog = DialogAddCountry.newInstance();
        addDialog.show(getFragmentManager(), getString(R.string.add_country));
    }

    public void removeCountries(View view) {
        Toast.makeText(this, mAdapter.getSelectedItemCount() + " " + getString(R.string.countries) + " "  + getResources().getString(R.string.removed), Toast.LENGTH_SHORT).show();
        mAdapter.removeItems(mAdapter.getSelectedItems());
        actionMode.finish();
    }

    public ArrayList<Country> getCountryList() {
        return countryList;
    }

    public ListAdapter getmAdapter() {
        return mAdapter;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (requestCode == 1)
                mAdapter.importList();
            else if (requestCode == 2)
                mAdapter.exportList();
            else if (requestCode == 3)
                displayOperatorInfo();
        }
    }
}