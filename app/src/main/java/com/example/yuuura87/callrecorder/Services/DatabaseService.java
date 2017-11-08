package com.example.yuuura87.callrecorder.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.yuuura87.callrecorder.DataBase.ChildRecordItem;
import com.example.yuuura87.callrecorder.DataBase.DatabaseAdapter;
import com.example.yuuura87.callrecorder.DataBase.ParentRecordingItem;

import java.util.ArrayList;

public class DatabaseService extends IntentService {

    public static final String TAG = "CallListenerService";

    public static final String ACTION_INSERT_PARENT = "com.example.yuuura87.callrecorder.service.action.ACTION_WRITE_PARENT";
    public static final String ACTION_INSERT_CHILD = "com.example.yuuura87.callrecorder.service.action.ACTION_WRITE_CHILD";
    public static final String ACTION_TRASH_PARENT = "com.example.yuuura87.callrecorder.service.action.ACTION_TRASH_PARENT";
    public static final String ACTION_TRASH_CHILD = "com.example.yuuura87.callrecorder.service.action.ACTION_TRASH_CHILD";
    public static final String ACTION_CLOSE_PARENT = "com.example.yuuura87.callrecorder.service.action.ACTION_CLOSE_PARENT";
    public static final String ACTION_DELETE_PARENT = "com.example.yuuura87.callrecorder.service.action.ACTION_DELETE_PARENT";
    public static final String ACTION_DELETE_CHILD = "com.example.yuuura87.callrecorder.service.action.ACTION_DELETE_CHILD";
    public static final String ACTION_MERGE_PARENTS = "com.example.yuuura87.callrecorder.service.action.ACTION_MOVE_CHILD_TO_DIFFERENT_PARENT";

    public static final String EXTRA_PARENT = "com.example.yuuura87.callrecorder.service.extra.EXTRA_PARENT";
    public static final String EXTRA_CHILD = "com.example.yuuura87.callrecorder.service.extra.EXTRA_CHILD";
    public static final String EXTRA_PARENTS = "com.example.yuuura87.callrecorder.service.extra.EXTRA_PARENTS";
    public static final String EXTRA_PARENT_ID = "com.example.yuuura87.callrecorder.service.extra.EXTRA_PARENT_ID";

    private DatabaseAdapter mDbAdapter = new DatabaseAdapter(this);

    public DatabaseService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            ParentRecordingItem p = null;
            ChildRecordItem c = null;
            long parentId = 0;
            ArrayList<ParentRecordingItem> pa = null;

            switch (action) {
                case ACTION_INSERT_PARENT:
                    p = intent.getParcelableExtra(EXTRA_PARENT);
                    handleActionInsertParent(p);
                    break;
                case ACTION_INSERT_CHILD:
                    c = intent.getParcelableExtra(EXTRA_CHILD);
                    handleActionInsertChild(c);
                    break;
                case ACTION_TRASH_PARENT:
                    p = intent.getParcelableExtra(EXTRA_PARENT);
                    handleActionTrashParent(p);
                    break;
                case ACTION_TRASH_CHILD:
                    c = intent.getParcelableExtra(EXTRA_CHILD);
                    handleActionTrashChild(c);
                    break;
                case ACTION_DELETE_PARENT:
                    p = intent.getParcelableExtra(EXTRA_PARENT);
                    handleActionDeleteParent(p);
                    break;
                case ACTION_DELETE_CHILD:
                    c = intent.getParcelableExtra(EXTRA_CHILD);
                    handleActionDeleteChild(c);
                    break;
                case ACTION_MERGE_PARENTS:
                    pa = intent.getParcelableArrayListExtra(EXTRA_PARENTS);
                    handleActionMergeParents(pa);
                    break;
                case ACTION_CLOSE_PARENT:
                    parentId = intent.getLongExtra(EXTRA_PARENT_ID, 0);
                    handleActionCloseParent(parentId);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleActionInsertParent(ParentRecordingItem p) {
        Log.d(TAG, "Inserting parent " + p);
        mDbAdapter.insertParentToDatabase(p);
    }

    private void handleActionInsertChild(ChildRecordItem c) {
        Log.d(TAG, "Inserting child " + c);
        mDbAdapter.insertChildToDatabase(c);

        ParentRecordingItem p = mDbAdapter.getParentOf(c.getParent());
        p.setNumActiveChildren(p.getNumActiveChildren() + 1);
        mDbAdapter.updateParentRecording(p);
    }

    private void handleActionTrashParent(ParentRecordingItem p) {
        Log.d(TAG, "Trashing parent " + p);
        int numChildTotal = p.getNumActiveChildren() + p.getNumTrashedChildren();
        p.setNumActiveChildren(0);
        p.setNumTrashedChildren(numChildTotal);
        p.setIsTrashed(true);
        mDbAdapter.updateParentRecording(p);
        ArrayList<ChildRecordItem> childRecordItems = mDbAdapter.getChildrenOf(p.getID(), false);
        if (childRecordItems != null) {
            for(ChildRecordItem c : childRecordItems) {
                c.setIsTrashed(true);
                mDbAdapter.updateChild(c);
            }
        }
    }

    private void handleActionTrashChild(ChildRecordItem c) {
        Log.d(TAG, "Trashing child " + c);
        c.setIsTrashed(true);
        ParentRecordingItem p = mDbAdapter.getParentOf(c.getParent());
        int active = p.getNumActiveChildren();
        int trashed = p.getNumTrashedChildren();
        active--;
        trashed++;
        p.setNumActiveChildren(active);
        p.setNumTrashedChildren(trashed);
        p.setIsTrashed(active == 0 ? true : false);
        mDbAdapter.updateParentRecording(p);
        mDbAdapter.updateChild(c);
    }

    private void handleActionDeleteParent(ParentRecordingItem p) {
        Log.d(TAG, "Deleting parent " + p);
        long pId = p.getID();
        mDbAdapter.deleteParent(pId);
    }

    private void handleActionDeleteChild(ChildRecordItem c) {
        Log.d(TAG, "Deleting child " + c);
        long cId = c.getID();
        mDbAdapter.deleteChild(cId);
        ParentRecordingItem p = mDbAdapter.getParentOf(c.getParent());
        int active = p.getNumActiveChildren();
        int trashed = p.getNumTrashedChildren();
        trashed--;
        if ((active == 0) && (trashed == 0)) {
            mDbAdapter.deleteParent(c.getParent());
        } else {
            p.setNumTrashedChildren(trashed);
            mDbAdapter.updateParentRecording(p);
        }
    }

    private void handleActionCloseParent(long parentId) {
        Log.d(TAG, "Closing parent " + parentId);
        ParentRecordingItem p = mDbAdapter.getParentOf(parentId);
        p.setIsClosed(true);
        mDbAdapter.updateParentRecording(p);

    }

    private void handleActionMergeParents(ArrayList<ParentRecordingItem> parents) {
        Log.d(TAG, "Merging parent " + parents);
    }
}