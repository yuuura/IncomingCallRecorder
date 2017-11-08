package com.example.yuuura87.callrecorder.DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.SQLException;

import java.util.ArrayList;

public class DatabaseAdapter {

    private static final String TAG = "DatabaseAdapter";
    DatabaseHelper helper;

    public DatabaseAdapter(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public ArrayList<ParentRecordingItem> getParentRecords(boolean isTrashed) {

        int trashed = isTrashed ? 1 : 0;


        ArrayList<ParentRecordingItem> parentRecordings = new ArrayList<>();

        String [] args = {Integer.toString(trashed)};
        String [] columns = {DatabaseHelper.PARENT_UID, DatabaseHelper.PARENT_NAME, DatabaseHelper.PARENT_OPENED_ON, DatabaseHelper.PARENT_CLOSED_ON, DatabaseHelper.PARENT_IS_CLOSED, DatabaseHelper.PARENT_IS_TRASHED, DatabaseHelper.PARENT_NUM_ACTIVE_CHILDREN, DatabaseHelper.PARENT_NUM_TRASHED_CHILDREN};
        String tableName = DatabaseHelper.TABLE_NAME_PARENT;
        SQLiteDatabase db = helper.getReadableDatabase();
        String orderBy = DatabaseHelper.PARENT_OPENED_ON + " DESC";

        Cursor res = null;
        try {
            res = db.query(tableName, columns, DatabaseHelper.PARENT_IS_TRASHED + " =?", args, null, null, orderBy , null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e(TAG, "finally");
        }

        if((res != null))
            while(res.moveToNext()) {
                ParentRecordingItem parent = new ParentRecordingItem();
                parent.setID(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_UID)));
                parent.setRecordName(res.getString(res.getColumnIndex(DatabaseHelper.PARENT_NAME)));
                parent.setStartTime(res.getString(res.getColumnIndex(DatabaseHelper.PARENT_OPENED_ON)));

                parent.setEndTime(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_CLOSED_ON)));
                parent.setIsClosed(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_IS_CLOSED)) > 0);
                parent.setIsTrashed(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_IS_TRASHED)) > 0);
                parent.setNumActiveChildren(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_NUM_ACTIVE_CHILDREN)));
                parent.setNumTrashedChildren(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_NUM_TRASHED_CHILDREN)));
                parentRecordings.add(parent);
            }

        Log.e(TAG, parentRecordings.toString());
        return parentRecordings;
    }

    public ParentRecordingItem getParentOf(long parentId) {

        ParentRecordingItem parent = new ParentRecordingItem();

        String [] args = {Long.toString(parentId)};
        String [] columns = {DatabaseHelper.PARENT_UID, DatabaseHelper.PARENT_NAME, DatabaseHelper.PARENT_OPENED_ON, DatabaseHelper.PARENT_CLOSED_ON, DatabaseHelper.PARENT_IS_CLOSED, DatabaseHelper.PARENT_IS_TRASHED, DatabaseHelper.PARENT_NUM_ACTIVE_CHILDREN, DatabaseHelper.PARENT_NUM_TRASHED_CHILDREN};
        String tableName = DatabaseHelper.TABLE_NAME_PARENT;

        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor res = null;
        try {
            res = db.query(tableName, columns, DatabaseHelper.PARENT_UID + " =?", args, null, null, null , null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e(TAG, "finally");
        }

        if((res != null))
            while(res.moveToNext()) {
                parent.setID(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_UID)));
                parent.setRecordName(res.getString(res.getColumnIndex(DatabaseHelper.PARENT_NAME)));
                parent.setStartTime(res.getString(res.getColumnIndex(DatabaseHelper.PARENT_OPENED_ON)));
                parent.setEndTime(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_CLOSED_ON)));
                parent.setIsClosed(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_IS_CLOSED)) > 0);
                parent.setIsTrashed(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_IS_TRASHED)) > 0);
                parent.setNumActiveChildren(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_NUM_ACTIVE_CHILDREN)));
                parent.setNumTrashedChildren(res.getInt(res.getColumnIndex(DatabaseHelper.PARENT_NUM_TRASHED_CHILDREN)));
            }

        Log.e(TAG, parent.toString());
        return parent;
    }

    public long insertParentToDatabase(ParentRecordingItem parent) {

        long id = -1;

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.PARENT_NAME, parent.getRecordName());
            contentValues.put(DatabaseHelper.PARENT_OPENED_ON, parent.getStartTime());
            if (parent.getIsClosed()) {
                contentValues.put(DatabaseHelper.PARENT_CLOSED_ON, parent.getEndTime());
            } else {
                contentValues.putNull(DatabaseHelper.PARENT_CLOSED_ON);
            }
            contentValues.put(DatabaseHelper.PARENT_IS_CLOSED, parent.getIsClosed());
            contentValues.put(DatabaseHelper.PARENT_IS_TRASHED, parent.getIsTrashed());
            contentValues.put(DatabaseHelper.PARENT_NUM_ACTIVE_CHILDREN, parent.getNumActiveChildren());
            contentValues.put(DatabaseHelper.PARENT_NUM_TRASHED_CHILDREN, parent.getNumTrashedChildren());
            id = db.insert(DatabaseHelper.TABLE_NAME_PARENT, null, contentValues);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return id;
    }

    public void updateParentRecording(ParentRecordingItem parentToUpdate) {

        String table = DatabaseHelper.TABLE_NAME_PARENT;
        String [] args = {Long.toString(parentToUpdate.getID())};
        String whereClause = DatabaseHelper.PARENT_UID + " =?";

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.PARENT_NAME, parentToUpdate.getRecordName());
            contentValues.put(DatabaseHelper.PARENT_OPENED_ON, parentToUpdate.getStartTime());
            if (parentToUpdate.getIsClosed()) {
                contentValues.put(DatabaseHelper.PARENT_CLOSED_ON, parentToUpdate.getEndTime());
            } else {
                contentValues.putNull(DatabaseHelper.PARENT_CLOSED_ON);
            }
            contentValues.put(DatabaseHelper.PARENT_IS_CLOSED, parentToUpdate.getIsClosed());
            contentValues.put(DatabaseHelper.PARENT_IS_TRASHED, parentToUpdate.getIsTrashed());
            contentValues.put(DatabaseHelper.PARENT_NUM_ACTIVE_CHILDREN, parentToUpdate.getNumActiveChildren());
            contentValues.put(DatabaseHelper.PARENT_NUM_TRASHED_CHILDREN, parentToUpdate.getNumTrashedChildren());
            db.update(table, contentValues, whereClause, args);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void insertChildToDatabase(ArrayList<DatabaseObject> children) {
        for(DatabaseObject c : children) {
            insertChildToDatabase(c);
        }
    }

    public long insertChildToDatabase(DatabaseObject obj) {

        long id = -1;
        ChildRecordItem childObj = (ChildRecordItem) obj;

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.CHILD_PARENT, childObj.getParent());
            contentValues.put(DatabaseHelper.CHILD_CALLER, childObj.getCaller());
            contentValues.put(DatabaseHelper.CHILD_PHONE_NUMBER, childObj.getPhoneNumber());
            contentValues.put(DatabaseHelper.CHILD_RECEIVED_ON, childObj.getCallTime());
            contentValues.put(DatabaseHelper.CHILD_IS_SMS, childObj.getIsSMS());
            if (childObj.getIsSMS()) {
                contentValues.put(DatabaseHelper.CHILD_MESSAGE, childObj.getMessage());
            } else {
                contentValues.putNull(DatabaseHelper.CHILD_MESSAGE);
            }
            contentValues.put(DatabaseHelper.CHILD_IS_TRASHED, childObj.getIsTrashed());
            id = db.insert(DatabaseHelper.TABLE_NAME_CHILDREN, null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return id;
    }

    public ArrayList<ChildRecordItem> getChildrenOf(long parent, boolean isTrashed) {

        int trashed = isTrashed ? 1 : 0;

        ArrayList<ChildRecordItem> childRecordings = new ArrayList<>();

        String [] args = {Long.toString(parent), Integer.toString(trashed)};
        String [] columns = {DatabaseHelper.CHILD_UID, DatabaseHelper.CHILD_PARENT, DatabaseHelper.CHILD_CALLER, DatabaseHelper.CHILD_PHONE_NUMBER, DatabaseHelper.CHILD_RECEIVED_ON, DatabaseHelper.CHILD_IS_SMS, DatabaseHelper.CHILD_MESSAGE, DatabaseHelper.CHILD_IS_TRASHED};
        String tableName = DatabaseHelper.TABLE_NAME_CHILDREN;
        String whereClause =  DatabaseHelper.CHILD_PARENT + " =? AND " + DatabaseHelper.CHILD_IS_TRASHED + " =?";
        SQLiteDatabase db = helper.getReadableDatabase();
        String orderBy = DatabaseHelper.CHILD_RECEIVED_ON + " DESC";

        Cursor res = null;
        try {
            res = db.query(tableName, columns, whereClause, args, null, null, orderBy , null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e(TAG, "finally");
        }

        if((res != null)) {
            while(res.moveToNext()) {
                ChildRecordItem child = new ChildRecordItem();
                child.setID(res.getInt(res.getColumnIndex(DatabaseHelper.CHILD_UID)));
                child.setParent(res.getInt(res.getColumnIndex(DatabaseHelper.CHILD_PARENT)));
                child.setCaller(res.getString(res.getColumnIndex(DatabaseHelper.CHILD_CALLER)));
                child.setPhoneNumber(res.getString(res.getColumnIndex(DatabaseHelper.CHILD_PHONE_NUMBER)));
                child.setCallTime(res.getString(res.getColumnIndex(DatabaseHelper.CHILD_RECEIVED_ON)));
                child.setIsSMS(res.getInt(res.getColumnIndex(DatabaseHelper.CHILD_IS_SMS)) > 0);
                child.setMessage(res.getString(res.getColumnIndex(DatabaseHelper.CHILD_MESSAGE)));
                child.setIsTrashed(res.getInt(res.getColumnIndex(DatabaseHelper.CHILD_IS_TRASHED)) > 0);
                childRecordings.add(child);
            }
        }

        Log.e(TAG, childRecordings.toString());
        return childRecordings;
    }

    public void updateChild(ChildRecordItem childObj) {

        String table = DatabaseHelper.TABLE_NAME_CHILDREN;
        String [] args = {Long.toString(childObj.getID())};
        String whereClause = DatabaseHelper.CHILD_UID + " =?";

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.CHILD_PARENT, childObj.getParent());
            contentValues.put(DatabaseHelper.CHILD_CALLER, childObj.getCaller());
            contentValues.put(DatabaseHelper.CHILD_PHONE_NUMBER, childObj.getPhoneNumber());
            contentValues.put(DatabaseHelper.CHILD_RECEIVED_ON, childObj.getCallTime());
            contentValues.put(DatabaseHelper.CHILD_IS_SMS, childObj.getIsSMS());
            if (childObj.getIsSMS()) {
                contentValues.put(DatabaseHelper.CHILD_MESSAGE, childObj.getMessage());
            } else {
                contentValues.putNull(DatabaseHelper.CHILD_MESSAGE);
            }
            contentValues.put(DatabaseHelper.CHILD_IS_TRASHED, childObj.getIsTrashed());
            db.update(table, contentValues, whereClause, args);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteParent(long parentId) {

        String table = DatabaseHelper.TABLE_NAME_PARENT;
        String [] args = {Long.toString(parentId)};
        String whereClause = DatabaseHelper.PARENT_UID + " =?";

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(table, whereClause, args);
            table = DatabaseHelper.TABLE_NAME_CHILDREN;
            whereClause = DatabaseHelper.CHILD_PARENT + " =?";
            db.delete(table, whereClause, args);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteChild(long childId) {

        String table = DatabaseHelper.TABLE_NAME_CHILDREN;
        String [] args = {Long.toString(childId)};
        String whereClause = DatabaseHelper.CHILD_UID + " =?";

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(table, whereClause, args);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    // Database SQLiteOpenHelper inner class
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String LOG = "DatabaseHelper";

        // Database Version
        private static final int DATABASE_VERSION = 12;

        // Database Name
        private static final String DATABASE_NAME = "call_recorder.db";

        // Records tables name
        private static final String TABLE_NAME_PARENT = "parentRecordingTable";
        private static final String TABLE_NAME_CHILDREN = "childRecordingTable";

        // parent table column names
        private static final String PARENT_UID = "_id";
        private static final String PARENT_NAME = "mRecordName";
        private static final String PARENT_OPENED_ON = "mStartTime";
        private static final String PARENT_CLOSED_ON = "mEndTime";
        private static final String PARENT_IS_CLOSED = "mIsClosed";
        private static final String PARENT_IS_TRASHED = "mIsTrashed";
        private static final String PARENT_NUM_ACTIVE_CHILDREN = "mNumActiveChildren";
        private static final String PARENT_NUM_TRASHED_CHILDREN = "mNumTrashedChildren";

        // child table column names
        private static final String CHILD_UID = "_id";
        private static final String CHILD_PARENT = "mParent";
        private static final String CHILD_CALLER = "mCaller";
        private static final String CHILD_PHONE_NUMBER = "mPhoneNumber";
        private static final String CHILD_RECEIVED_ON = "mCallTime";
        private static final String CHILD_IS_SMS = "mIsSMS";
        private static final String CHILD_MESSAGE = "mMessage";
        private static final String CHILD_IS_TRASHED = "mIsTrashed";

        // Column characteristics
        private static final String UID_TYPE = " INTEGER";
        private static final String CONTACT_DETAIL_TYPE = " VARCHAR(255)";
        private static final String ENTERED_ON_TYPE = " INTEGER"; // time in milliseconds
        private static final String CLOSED_ON_TYPE = " INTEGER"; // time in milliseconds
        //private static final String OPENED_ON_TYPE = " INTEGER"; // time in milliseconds
        private static final String OPENED_ON_TYPE = " VARCHAR(255)"; // time in milliseconds

        private static final String IS_SMS_TYPE = " INTEGER"; // 0 false 1 true
        private static final String IS_TRASHED_TYPE = " INTEGER"; // 0 false 1 true
        private static final String IS_CLOSED_TYPE = " INTEGER"; // 0 false 1 true
        private static final String NUMBER_ACTIVE_CHILDREN = " INTEGER";
        private static final String NUMBER_TRASHED_CHILDREN = " INTEGER";
        private static final String MESSAGE_TYPE = " TEXT";
        private static final String NAME_TYPE = " VARCHAR(255)";
        private static final String COMMA_SEP = ",";

        private static final String SQL_DELETE_PARENT_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME_PARENT;
        private static final String SQL_DELETE_CHILD_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME_CHILDREN;


        // create table strings
        private static final String CREATE_PARENT_TABLE = "CREATE TABLE "
                + TABLE_NAME_PARENT + " ("
                + PARENT_UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PARENT_NAME + NAME_TYPE + COMMA_SEP
                + PARENT_OPENED_ON + OPENED_ON_TYPE + COMMA_SEP
                + PARENT_CLOSED_ON + CLOSED_ON_TYPE + COMMA_SEP
                + PARENT_IS_CLOSED + IS_CLOSED_TYPE + COMMA_SEP
                + PARENT_IS_TRASHED + IS_TRASHED_TYPE + COMMA_SEP
                + PARENT_NUM_ACTIVE_CHILDREN + NUMBER_ACTIVE_CHILDREN + COMMA_SEP
                + PARENT_NUM_TRASHED_CHILDREN + NUMBER_TRASHED_CHILDREN + ")";

        private static final String CREATE_CHILDREN_TABLE = "CREATE TABLE "
                + TABLE_NAME_CHILDREN + " ("
                + CHILD_UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CHILD_PARENT + UID_TYPE + COMMA_SEP
                + CHILD_CALLER + CONTACT_DETAIL_TYPE + COMMA_SEP
                + CHILD_PHONE_NUMBER + CONTACT_DETAIL_TYPE + COMMA_SEP
                + CHILD_RECEIVED_ON + ENTERED_ON_TYPE + COMMA_SEP
                + CHILD_IS_SMS + IS_SMS_TYPE + COMMA_SEP
                + CHILD_MESSAGE + MESSAGE_TYPE + COMMA_SEP
                + CHILD_IS_TRASHED + IS_TRASHED_TYPE + COMMA_SEP
                + " FOREIGN KEY " + "(" + CHILD_PARENT + ") REFERENCES " + TABLE_NAME_PARENT + "(" + PARENT_UID + ")" + ")";

        private static DatabaseHelper sInstance;

        public static synchronized DatabaseHelper getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new DatabaseHelper(context.getApplicationContext());
            }
            return sInstance;
        }

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_PARENT_TABLE);
                db.execSQL(CREATE_CHILDREN_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Log.d(LOG, "db created");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_PARENT_ENTRIES);
            db.execSQL(SQL_DELETE_CHILD_ENTRIES);
            Log.d(LOG, "db onUpgrade");
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

    }
}
