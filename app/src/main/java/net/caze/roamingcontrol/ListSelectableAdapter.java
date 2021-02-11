package net.caze.roamingcontrol;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

abstract class ListSelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    @SuppressWarnings("unused")
    private static final String TAG = ListSelectableAdapter.class.getSimpleName();

    private SparseBooleanArray selectedItems;

    ListSelectableAdapter() {
        selectedItems = new SparseBooleanArray();
    }
    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }
    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }
    /**
     * Clear the selection status for all items
     */
    void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }
    /**
     * Count the selected items
     * @return Selected items count
     */
    int getSelectedItemCount() {
        return selectedItems.size();
    }
    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    ArrayList<Integer> getSelectedItems() {
        ArrayList<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}
