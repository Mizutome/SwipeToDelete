package net.sh026125.swipetodelete.RecyclerViewSwipe;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Mizu  on 2016/11/15.
 *
 *  RecyclerView Adapter cam extends this class, and override necessary function to use "Swipe Delete"
 */

public abstract class RecyclerViewSwipeAdapter extends RecyclerView.Adapter {
    protected static final int LIST_POSITION_NULL = -1;
    protected int deletePosition = LIST_POSITION_NULL;

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(deletePosition == position){   // Set for swipe delete
            onBindViewHolderSwipedView(holder, position);
        }
        else {
            onBindViewHolderNormalView(holder, position);
        }
    }

    public void onSwiped(int listPosition){
        if(deletePosition == LIST_POSITION_NULL){
            deletePosition = listPosition;
            notifyItemChanged(listPosition);
        }
        else{
            if(deletePosition == listPosition) {
                remove(deletePosition);    // auto-delete the deletePosition item
                deletePosition = LIST_POSITION_NULL;
            }
            else{
                remove(deletePosition);    // auto-delete the deletePosition item
                if (listPosition > deletePosition)
                    listPosition--;    // Because there is an item(in deletePosition) that has been deleted(removed)
                deletePosition = listPosition;
                notifyItemChanged(listPosition);
            }
        }
    }

    protected abstract void remove(int position);                                       // Call this function to do remove elements in RecyclerView Adapter
    protected abstract void onBindViewHolderNormalView(RecyclerView.ViewHolder holder, int position);  // Decorate Normal ViewHolder for onBindViewHolder()
    protected abstract void onBindViewHolderSwipedView(RecyclerView.ViewHolder holder, int position);  // Decorate Swiped ViewHolder for onBindViewHolder()
}
