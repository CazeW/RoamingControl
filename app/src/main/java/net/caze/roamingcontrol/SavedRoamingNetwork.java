package net.caze.roamingcontrol;


import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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


public class SavedRoamingNetwork extends AppCompatActivity implements ListAdapter.MyViewHolder.ClickListener, SharedPreferencesValues {
    private ArrayList<Network> networkList;
    private NetworkInfo networkInfo;
    private ListAdapter mAdapter;
    private Network currentNetwork;

    private RecyclerView mRecyclerView;
    private Button simselect;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedroaming_network);

        //For dualSIM
        simselect = (Button) findViewById(R.id.select_sim);
        if (!MainActivity.isDualSIM()) {
            simselect.setVisibility(View.GONE);
        } else {
            MainActivity.sharedPref.edit().putInt(KEY_PREF_SIM_SELECTED, 0).apply();
        }

        if (getIntent().getStringExtra("FORCE_OR_NETWORK").equals("NETWORK")) {
            mAdapter = new ListAdapter(this, NETWORK);
        } else {
            mAdapter = new ListAdapter(this, FORCE);
            setTitle(getString(R.string.force_roaming));
        }
        networkList = mAdapter.getList();

        mRecyclerView = (RecyclerView) findViewById(R.id.networklist);
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
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.sim1 && MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0) != 0) {
                    MainActivity.sharedPref.edit().putInt(KEY_PREF_SIM_SELECTED, 0).apply();
                    displayOperatorInfo();
                    simselect.setText(getString(R.string.sim1));
                    mAdapter.switchSIMList(0);
                    networkList = mAdapter.getList();
                } else if (id == R.id.sim2 && MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0) != 1) {
                    MainActivity.sharedPref.edit().putInt(KEY_PREF_SIM_SELECTED, 1).apply();
                    displayOperatorInfo();
                    simselect.setText(getString(R.string.sim2));
                    mAdapter.switchSIMList(1);
                    networkList = mAdapter.getList();
                }
                return true;
            }
        });
        popup.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_saved_network, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort:
                View menuItemView = findViewById(id);
                PopupMenu popup = new PopupMenu(this, menuItemView);
                popup.inflate(R.menu.popup_sort);

                MenuItem sortname = popup.getMenu().findItem(R.id.sort_name);
                MenuItem sortcountry = popup.getMenu().findItem(R.id.sort_country);
                if (mAdapter.getSortOrder()) {
                    sortname.setChecked(true);
                } else {
                    sortcountry.setChecked(true);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.sort_name) {
                            item.setChecked(true);
                            mAdapter.setSortOrder(true);
                        } else {
                            item.setChecked(true);
                            mAdapter.setSortOrder(false);
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
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
                DialogFragment helpDialog;
                if (getIntent().getStringExtra("FORCE_OR_NETWORK").equals("NETWORK"))
                    helpDialog = DialogHelp.newInstance(NETWORK);
                else
                    helpDialog = DialogHelp.newInstance(FORCE);
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
            DialogFragment editDialog = DialogEdit.newInstance(position);
            editDialog.show(getFragmentManager(), "dialog");
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
        TextView currentMnc = (TextView) findViewById(R.id.currentmnc);

        /*if (MainActivity.isDualSIM() && !PermissionHandler.isPhoneStatePermissionGranted(this))
            currentNetwork = new Network(getString(R.string.unknown), "", "---", "---");
        else
            currentNetwork = networkInfo.getCurrentNetwork(this);*/

        currentNetwork = networkInfo.getCurrentNetwork(this);

        if (currentNetwork.getCountry().equals("")) {
            currentName.setText(getString(R.string.current_network_name_country, currentNetwork.getName(), ""));
            currentMcc.setText(currentNetwork.getMcc_text());
            currentMnc.setText(currentNetwork.getMnc_text());
        } else {
            currentName.setText(getString(R.string.current_network_name_country, currentNetwork.getName(), currentNetwork.getCountry_text()));
            currentMcc.setText(currentNetwork.getMcc_text());
            currentMnc.setText(currentNetwork.getMnc_text());
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

    public void addCurrentNetwork(View view) {
        if (!mAdapter.listContains(false, networkList, currentNetwork, null)) {
            if (currentNetwork.getMcc().equals("---")) {
                Toast.makeText(this, getString(R.string.unknown_network), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.current_network_added), Toast.LENGTH_SHORT).show();
                mAdapter.add(currentNetwork);
            }
        } else {
            Toast.makeText(this, getString(R.string.current_in_list), Toast.LENGTH_SHORT).show();
        }

    }

    public void addNewNetwork(View view) {
        DialogFragment addDialog = DialogAddNetwork.newInstance(false, 0);
        addDialog.show(getFragmentManager(), getString(R.string.add_network));
    }

    public void removeNetworks(View view) {
        Toast.makeText(this, mAdapter.getSelectedItemCount() + " " + getString(R.string.networks) + " " + getString(R.string.removed), Toast.LENGTH_SHORT).show();
        mAdapter.removeItems(mAdapter.getSelectedItems());
        actionMode.finish();
    }

    public ArrayList<Network> getNetworkList() {
        return networkList;
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