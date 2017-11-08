package com.example.yuuura87.callrecorder.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;

import com.example.yuuura87.callrecorder.DataBase.ChildRecordItem;
import com.example.yuuura87.callrecorder.DataBase.DatabaseAdapter;
import com.example.yuuura87.callrecorder.DataBase.ParentRecordingItem;
import com.example.yuuura87.callrecorder.Receivers.OnFileCreatedForExportReceiver;
import com.example.yuuura87.callrecorder.Utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CommaSeparatedValuesService extends IntentService {

    public static final String TAG = "CommaSeparatedValuesService";

    public static final String ACTION_WRITE_TO_FILE = "com.example.yuuura87.callrecorder.service.action.ACTION_EXPORT_TO_MAIL";

    public static final String EXTRA_PARENT = "com.example.yuuura87.callrecorder.service.extra.PARENT";
    public static final String EXTRA_TRASHED = "com.example.yuuura87.callrecorder.service.extra.TRASHED";

    public static final String COMMA_DELIMITER = ",";
    public static final String NEW_LINE_SEPERATOR = "\n";

    public static final String CSV_FILE_HEADER = "caller" + COMMA_DELIMITER + "phone number" + COMMA_DELIMITER
            + "Call date" + COMMA_DELIMITER + "SMS Message" + NEW_LINE_SEPERATOR;

    public static final String FILE_NAME="myFile.csv";

    public CommaSeparatedValuesService() {
        super("CommaSeparatedValuesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WRITE_TO_FILE.equals(action)) {
                boolean trashed = intent.getBooleanExtra(EXTRA_TRASHED, false);
                ArrayList<ParentRecordingItem> parents = intent.getParcelableArrayListExtra(EXTRA_PARENT);
                handleActionExport(parents, trashed);
            }
        }
    }

    private void handleActionExport(ArrayList<ParentRecordingItem> parents, boolean trashed) {

        ArrayList<ChildRecordItem> toWrite = getAllChildrenfromDB(parents, trashed);
        if(isWriteable()) {
            String path = writeToCsv(toWrite);
            broadcastFileReady(path);
        } else {
        }
    }

    private boolean isWriteable() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            return  true;
        } else {
            return false;
        }
    }

    private String writeToCsv(ArrayList<ChildRecordItem> children) {

        File sdCard = Environment.getExternalStorageDirectory();
        File f = new File(sdCard, FILE_NAME);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(CSV_FILE_HEADER.getBytes());
            Iterator<ChildRecordItem> it = children.iterator();
            while(it.hasNext()) {
                ChildRecordItem current = it.next();
                fos.write(current.getCaller().getBytes());
                fos.write(COMMA_DELIMITER.getBytes());
                fos.write(current.getPhoneNumber().getBytes());
                fos.write(COMMA_DELIMITER.getBytes());
                fos.write(current.getCallTime().getBytes());

                fos.write(COMMA_DELIMITER.getBytes());
                if(current.getMessage() != null) {
                    fos.write(current.getMessage().getBytes());
                }
                fos.write(NEW_LINE_SEPERATOR.getBytes());
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath().toString();
    }

    private void broadcastFileReady(String path) {
        Intent intent = new Intent();
        intent.setAction(OnFileCreatedForExportReceiver.ACTION_EXPORT_TO_MAIL);
        intent.putExtra(OnFileCreatedForExportReceiver.EXTRA_PATH, path);
        sendBroadcast(intent);
    }

    private ArrayList<ChildRecordItem> getAllChildrenfromDB(ArrayList<ParentRecordingItem> parents, boolean trashed) {

        DatabaseAdapter mDbAdapter = new DatabaseAdapter(this);

        ArrayList<ChildRecordItem> fullArray = new ArrayList<>();

        for(ParentRecordingItem p : parents) {
            ArrayList<ChildRecordItem> childRecordItems = mDbAdapter.getChildrenOf(p.getID(), trashed);
            fullArray.addAll(childRecordItems);
        }
        return fullArray;
    }


}