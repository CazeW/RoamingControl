package net.caze.roamingcontrol;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


class ListAdapter extends ListSelectableAdapter<ListAdapter.MyViewHolder> implements SharedPreferencesValues {
    private String SORT_ORDER;
    private static int type;

    private ArrayList list;
    private ArrayList list_sim1;
    private ArrayList list_sim2;
    private boolean sortOrder;
    private FileOperations fOperations;

    private MyViewHolder.ClickListener clickListener;

    ListAdapter(MyViewHolder.ClickListener clickListener, int types) {
        super();
        Context context = (Context) clickListener;
        type = types;
        this.fOperations = new FileOperations(context, this, type);
        this.clickListener = clickListener;
        if (MainActivity.isDualSIM()) {
            this.list_sim1 = fOperations.readFile(context, 1);
            this.list_sim2 = fOperations.readFile(context, 2);
            this.list = list_sim1;
        } else {
            this.list = fOperations.readFile(context, 0);
        }

        if (type == COUNTRY)
            SORT_ORDER = COUNTRY_SORT_ORDER;
        else if (type == FORCE)
            SORT_ORDER = FORCE_SORT_ORDER;
        else
            SORT_ORDER = NETWORK_SORT_ORDER;

        sortOrder = MainActivity.sharedPref.getBoolean(SORT_ORDER, true);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (type == COUNTRY)
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.country_list_row, parent, false);
        else
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.network_list_row, parent, false);
        return new MyViewHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        if (type == COUNTRY) {
            Country country = (Country) list.get(position);
            holder.name.setText(country.getName());
            holder.mcc.setText(country.getMcc_text());
        } else {
            Network network = (Network) list.get(position);
            holder.name.setText(network.getName());
            holder.country.setText(network.getCountry_text());
            holder.mcc.setText(network.getMcc_text());
            holder.mnc.setText(network.getMnc_text());
        }
        // Highlight the item if it's selected
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        RelativeLayout layout;
        TextView name, country, mcc, mnc;
        View selectedOverlay;

        private ClickListener listener;

        MyViewHolder(View view, ClickListener listener) {
            super(view);
            if (type == COUNTRY) {
                layout = (RelativeLayout) view.findViewById(R.id.country);
                name = (TextView) view.findViewById(R.id.countryname);
                mcc = (TextView) view.findViewById(R.id.countrymcc);
            } else {
                layout = (RelativeLayout) view.findViewById(R.id.network);
                name = (TextView) view.findViewById(R.id.networkname);
                country = (TextView) view.findViewById(R.id.networkcountry);
                mcc = (TextView) view.findViewById(R.id.networkmcc);
                mnc = (TextView) view.findViewById(R.id.networkmnc);
            }
            selectedOverlay = view.findViewById(R.id.selected_overlay);

            this.listener = listener;

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getLayoutPosition());
            }
            return false;
        }

        interface ClickListener {
            void onItemClicked(int position);
            boolean onItemLongClicked(int position);
        }

    }

    void add(Object object) {
        list.add(object);
        sortList();
        notifyItemInserted(list.indexOf(object));
        fOperations.updateFile(list);
    }

    void edit(int position, Object object) {
        list.set(position, object);
        sortList();
        int newpos = list.indexOf(object);
        if (position == newpos) {
            notifyItemChanged(position);
        } else {
            notifyItemRemoved(position);
            notifyItemInserted(newpos);
        }
        fOperations.updateFile(list);
    }

    void remove(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        fOperations.updateFile(list);
    }

    void removeItems(ArrayList<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                remove(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }

                if (count == 1) {
                    remove(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }

                for (int i = 0; i < count; ++i) {
                    positions.remove(0);
                }
            }
        }
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            list.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
        fOperations.updateFile(list);
    }

    ArrayList getList() {
        sortList();
        return list;
    }

    void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
        MainActivity.sharedPref.edit().putBoolean(SORT_ORDER, sortOrder).apply();
        updateList(this.list);
    }

    boolean getSortOrder() {
        return sortOrder;
    }

    private void sortList() {
        if (type == COUNTRY) {
            Collections.sort(list, Country.nameComparator);
        } else {
            if (sortOrder)
                Collections.sort(list, Network.nameComparator);
            else
                Collections.sort(list, Network.countryComparator);
        }
    }

    void updateList(ArrayList list) {
        this.list = list;
        sortList();
        notifyDataSetChanged();
    }

    void switchSIMList(int toList) {
        if (toList == 0) {
            list_sim2 = list;
            list = list_sim1;
        } else {
            list_sim1 = list;
            list = list_sim2;
        }
        sortList();
        notifyDataSetChanged();
    }

    void importList() {
        fOperations.importFile();
    }

    void exportList() {
        fOperations.exportFile();
    }

    boolean listContains(boolean mode, ArrayList list, Object newO, Object skipO) {
        if (type == COUNTRY) {
            Country newCountry = (Country) newO;
            for (Object obj : list) {
                Country country = (Country) obj;
                if (country.getMcc().equals(newCountry.getMcc())) {
                    return true;
                }
            }
            return false;
        }
        //True for comparing name, mcc and mnc. False for comparing mcc and mnc
        else if (mode) {
            Network newNetwork = (Network) newO;
            for (Object obj : list) {
                Network network = (Network) obj;
                if (network.getNetwork().equals(newNetwork.getNetwork())) {
                    return true;
                }
                else if (network.getName().equalsIgnoreCase(newNetwork.getName()) && network.getMcc().equals(newNetwork.getMcc())) {
                    return true;
                }
            }
            return false;

        } else {
            Network newNetwork = (Network) newO;
            for (Object obj : list) {
                Network network = (Network) obj;
                if (skipO != null) {
                    if (network.getNetwork().equals(newNetwork.getNetwork()) &&
                            !network.getNetwork().equals(newNetwork.getNetwork())) {
                        return true;
                    }
                } else {
                    if (network.getNetwork().equals(newNetwork.getNetwork())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
