package com.example.yuuura87.callrecorder.Adapters;

import com.example.yuuura87.callrecorder.DataBase.ParentRecordingItem;

public interface AdapterListener {

    void onContextMenuState();
    void onDeselectingAllItems();
    void onChildrenRequest(ParentRecordingItem parent, boolean trashedChildren);

}
