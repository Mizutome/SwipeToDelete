package net.sh026125.swipetodelete.RecyclerViewSwipe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import net.sh026125.swipetodelete.R;

/**
 * Created by Mizu on 2016/10/27.
 *
 *  Build Swipe function in RecyclerView
 */

public class RecyclerViewSwipe {

    public static void build(final Context mContext, final RecyclerView recyclerView) {
        setUpItemTouchHelper(mContext, recyclerView);
        setUpAnimationDecoratorHelper(recyclerView);
    }

    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private static void setUpItemTouchHelper(final Context mContext, final RecyclerView recyclerView) {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            RecyclerViewSwipeAdapter adapter;
            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable iconMark;
            int iconMarkMargin;
            boolean initiated;

            private void init() {
                adapter = (RecyclerViewSwipeAdapter)recyclerView.getAdapter();
                background = new ColorDrawable(Color.RED);
                iconMark = ContextCompat.getDrawable(mContext, R.drawable.ic_archive_24dp);
                iconMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                iconMarkMargin = (int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                adapter.onSwiped(swipedPosition);
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                if(dX < 0) {    // Right Swiped
                    // draw red background
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                    // draw icon
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int intrinsicWidth = iconMark.getIntrinsicWidth();
                    int intrinsicHeight = iconMark.getIntrinsicWidth();

                    int iconMarkLeft = itemView.getRight() - iconMarkMargin - intrinsicWidth;
                    int iconMarkRight = itemView.getRight() - iconMarkMargin;
                    int iconMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int iconMarkBottom = iconMarkTop + intrinsicHeight;
                    iconMark.setBounds(iconMarkLeft, iconMarkTop, iconMarkRight, iconMarkBottom);
                }
                else if(dX > 0) {        // Left Swiped
                    // draw red background
                    background.setBounds(0, itemView.getTop(), (int) dX, itemView.getBottom());

                    // draw icon
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int intrinsicWidth = iconMark.getIntrinsicWidth();
                    int intrinsicHeight = iconMark.getIntrinsicWidth();

                    int iconMarkLeft = iconMarkMargin;
                    int iconMarkRight = iconMarkMargin + intrinsicWidth;
                    int iconMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int iconMarkBottom = iconMarkTop + intrinsicHeight;
                    iconMark.setBounds(iconMarkLeft, iconMarkTop, iconMarkRight, iconMarkBottom);
                }

                background.draw(canvas);
                iconMark.draw(canvas);

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to their new positions
     * after an item is removed.
     */
    private static void setUpAnimationDecoratorHelper(final RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(canvas);
                }
                super.onDraw(canvas, parent, state);
            }
        });
    }
}
