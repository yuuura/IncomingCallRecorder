package com.example.yuuura87.callrecorder.Adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.ArrayList;

public class SimpleTouchCallback extends ItemTouchHelper.Callback {

    private ArrayList<OnSwipeListener> mListeners = new ArrayList<>();

    public SimpleTouchCallback(OnSwipeListener listener) {
        setListeners(listener);
    }

    public void setListeners(OnSwipeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        for (OnSwipeListener l : mListeners) {
            l.onSwipe(viewHolder.getLayoutPosition());
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
}
