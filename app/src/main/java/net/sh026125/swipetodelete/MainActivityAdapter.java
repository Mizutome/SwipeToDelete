package net.sh026125.swipetodelete;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.sh026125.swipetodelete.RecyclerViewSwipe.RecyclerViewSwipeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class MainActivityAdapter extends RecyclerViewSwipeAdapter{

    List<String> items;

    int lastInsertedIndex; // so we can add some more items for testing purposes

    public MainActivityAdapter(){
        items = new ArrayList<>();

        // let's generate some items
        lastInsertedIndex = 15;
        // this should give us a couple of screens worth
        for (int i=1; i<= lastInsertedIndex; i++) {
            items.add("Item " + i);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RowViewHolder(parent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    protected void remove(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    protected void onBindViewHolderNormalView(RecyclerView.ViewHolder holder, int position) {
        RowViewHolder viewHolder = (RowViewHolder)holder;

        viewHolder.textViewMessage.setVisibility(View.VISIBLE);
        viewHolder.layoutSwiped.setVisibility(View.GONE);

        viewHolder.textViewMessage.setText(items.get(position));
    }

    @Override
    protected void onBindViewHolderSwipedView(RecyclerView.ViewHolder holder, final int position) {
        RowViewHolder viewHolder = (RowViewHolder)holder;

        viewHolder.textViewMessage.setVisibility(View.GONE);
        viewHolder.layoutSwiped.setVisibility(View.VISIBLE);

        viewHolder.buttonAchieved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(position);
                deletePosition = LIST_POSITION_NULL;
            }
        });
        viewHolder.buttonUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(position);
                deletePosition = LIST_POSITION_NULL;
            }
        });
    }

    /**
     *  Utility method to add some rows for testing purposes. You can add rows from the toolbar menu.
     */
    public void addItems(int howMany){
        if (howMany > 0) {
            for (int i = lastInsertedIndex + 1; i <= lastInsertedIndex + howMany; i++) {
                items.add("Item " + i);
                notifyItemInserted(items.size() - 1);
            }
            lastInsertedIndex = lastInsertedIndex + howMany;
        }
    }

    /**
     * ViewHolder capable of presenting two states: "achieved" and "undo" state.
     */
    static class RowViewHolder extends RecyclerView.ViewHolder {

        TextView textViewMessage;

        FrameLayout layoutSwiped;
        Button buttonAchieved;
        Button buttonUndo;

        public RowViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view, parent, false));
            textViewMessage = (TextView) itemView.findViewById(R.id.textview_message);

            layoutSwiped   = (FrameLayout) itemView.findViewById(R.id.layout_swiped);
            buttonAchieved = (Button) itemView.findViewById(R.id.button_achieved);
            buttonUndo     = (Button) itemView.findViewById(R.id.button_undo);
        }
    }
}
