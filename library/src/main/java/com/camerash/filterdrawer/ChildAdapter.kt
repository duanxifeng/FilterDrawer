package com.camerash.filterdrawer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Adapter for ChildItem used in the FilterDrawer
 *
 * @author Camerash
 * @param Parent Your custom parent type that extends ParentItem
 * @param Child Your custom child type that extends ChildItem
 * @param parent ParentItem hosting this ChildAdapter
 * @param childItemList List of ChildItem
 * @param callback Callback to ParentItem ViewHolder when selection changes. Boolean indicates whether
 * @see ChildItem
 * @see ParentAdapter
 */
class ChildAdapter<Parent, Child>(val parent: Parent, val childItemList: List<Child>, private val callback: (Set<Child>, Boolean) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() where Parent : ParentItem, Child : ChildItem {

    /**
     * A map for recoding selected items
     */
    private var selectedItemMap = mutableMapOf<Int, Child>()

    /**
     * Inflate view and create view holder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            childItemList.first().getViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.default_filter_child, viewGroup, false))

    /**
     * @return Number of child filters
     */
    override fun getItemCount(): Int = childItemList.size

    /**
     * Called when view holder request binding
     * Leave empty as we will be using the method that receives payloads
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    /**
     * Called when view holder request binding
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val childItem = childItemList[position]
        val vh = holder as ChildItem.ViewHolder

        if (payloads.isEmpty()) {
            vh.bindView(childItem) { onChildClicked(vh.adapterPosition, childItem) }
        } else {
            when (payloads.first()) {
                is Boolean -> {
                    if (payloads.first() as Boolean) {
                        vh.onSelect(childItem)
                    } else {
                        vh.onDeselect(childItem)
                    }
                }
                is Int -> {
                    // Reset
                    vh.onReset(childItem)
                }
            }
        }
    }

    /**
     * Called on child clicked
     * Perform logic on selected child filter
     */
    private fun onChildClicked(adapterPosition: Int, childItem: Child) {
        if (selectedItemMap.isEmpty()) {
            // Item selected
            notifyItemChanged(adapterPosition, true)
            selectedItemMap[adapterPosition] = childItem
            callback(getSelectedChildSet(), true)
        } else {
            val selectedChild = selectedItemMap[adapterPosition]
            if (selectedChild != null) {
                // Item deselected
                notifyItemChanged(adapterPosition, false)
                selectedItemMap.remove(adapterPosition)
                callback(getSelectedChildSet(), false)
            } else {
                // Select only one, and item map must contain one
                if (!parent.allowSelectMultiple()) {
                    // New item selected
                    // Deselect old item first
                    val lastSelectedIndex = selectedItemMap.iterator().next().key

                    notifyItemChanged(lastSelectedIndex, false)
                    selectedItemMap.remove(lastSelectedIndex)
                }
                // Select new item
                notifyItemChanged(adapterPosition, true)
                selectedItemMap[adapterPosition] = childItem
                callback(getSelectedChildSet(), true)
            }
        }
    }

    /**
     * Reset child filters
     */
    fun reset() {
        selectedItemMap.forEach {
            notifyItemChanged(it.key, RESET_FLAG)
        }
        selectedItemMap.clear()
    }

    /**
     * @return Set of selected child filters
     */
    fun getSelectedChildSet(): Set<Child> {
        val childSet = mutableSetOf<Child>()
        selectedItemMap.forEach { childSet.add(it.value) }
        return childSet
    }

    companion object {
        const val RESET_FLAG = -1
    }
}