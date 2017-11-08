package com.example.yuuura87.callrecorder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuuura87.callrecorder.Adapters.AdapterListener;
import com.example.yuuura87.callrecorder.Adapters.RecordingDataAdapter;
import com.example.yuuura87.callrecorder.Adapters.SimpleTouchCallback;
import com.example.yuuura87.callrecorder.Constants.AppConstants;
import com.example.yuuura87.callrecorder.DataBase.DatabaseAdapter;
import com.example.yuuura87.callrecorder.DataBase.DatabaseObject;
import com.example.yuuura87.callrecorder.DataBase.ParentRecordingItem;
import com.example.yuuura87.callrecorder.Receivers.OnFileCreatedForExportReceiver;
import com.example.yuuura87.callrecorder.Services.CallListenerService;
import com.example.yuuura87.callrecorder.Services.CommaSeparatedValuesService;
import com.example.yuuura87.callrecorder.Services.NotificationService;
import com.example.yuuura87.callrecorder.Services.RecordFlowIntentService;
import com.example.yuuura87.callrecorder.Utils.Utils;
import com.example.yuuura87.callrecorder.Widget.CallRecorderRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // constants
    private static final String TAG = "MainActivity";

    // UI components
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;

    private Toolbar mToolbar;
    private FloatingActionButton mFab;

    private CallRecorderRecyclerView mRecyclerView;

    private View mEmptyView;
    private TextView mEmptyViewText;

    private String mDefaultReturnMessage;
    private int[] mRemindTimeSet;

    //private NotificationActivity notificationActivity;

    /*private Switch swEnable;*/
    private boolean mIsNotificationsEnabled = false;

    // data and data adapters components
    private DatabaseAdapter mDBAdapter;
    private RecordingDataAdapter mAdapter;
    private ArrayList<? extends DatabaseObject> mData = new ArrayList<>();

    // receivers
    OnFileCreatedForExportReceiver mNewsFileReadyReceiver = new OnFileCreatedForExportReceiver();

    // App state variables
    private boolean mIsRecording;

    // activity state variables
    private int mActiveNavMenuItemId;
   // private int mOptionMenuTemId;
    private boolean mIsOnActionMode;
    private long mWatchedParentId;
    private ArrayList<ParentRecordingItem> mSelected = new ArrayList<>();

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
    };

    private boolean mFileAccessPermission;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            } else {
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFileAccessPermission = false;
                }else {
                    mFileAccessPermission = true;
                }
                updateSharedRecordingFilePermission(mFileAccessPermission);
                return;
            }
        }
    }

    // Action mode, callbacks and listeners
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.paren_recording_data_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
            switch (menuItem.getItemId()) {
                case R.id.menu_item_delete:
                    for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                        mAdapter.deleteItem(selectedItemPositions.get(i));
                    }
                    actionMode.finish();
                    return true;
                case R.id.menu_item_share:
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        ArrayList<ParentRecordingItem> toExport = extractParentsFromSelectedSparseArray();
                        Intent intent = new Intent(MainActivity.this, CommaSeparatedValuesService.class);
                        intent.setAction(CommaSeparatedValuesService.ACTION_WRITE_TO_FILE);
                        intent.putParcelableArrayListExtra(CommaSeparatedValuesService.EXTRA_PARENT, toExport);
                        intent.putExtra(CommaSeparatedValuesService.EXTRA_TRASHED, mActiveNavMenuItemId == R.id.nav_trash);
                        startService(intent);
                    }
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mIsOnActionMode = false;
            mAdapter.clearSelection();
            mActionMode = null;
        }
    };

    private AdapterListener mAdapterListener = new AdapterListener() {

        @Override
        public void onContextMenuState() {
            openActionMode();
        }

        @Override
        public void onDeselectingAllItems() {
            closeActionMode();
        }

        @Override
        public void onChildrenRequest(ParentRecordingItem parent, boolean trashedChildren) {
            setEmptyViewTitle("No more recordings here");
            mWatchedParentId = parent.getID();
            mAdapter.clearSelection();
            mData = mDBAdapter.getChildrenOf(mWatchedParentId, trashedChildren);
            mAdapter.update(mData);
            mNavigationView.getMenu().findItem(R.id.nav_recordings).setChecked(false);
            invalidateOptionsMenu();
        }
    };

    private void openActionMode() {
        mIsOnActionMode = true;
        if (mActionMode != null) {
            return;
        }
        mActionMode = startSupportActionMode(mActionModeCallback);
    }

    public void closeActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private String loadReturnMessageFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstants.SHARED_PREFS_RETURN_MESSAGE_KEY, mDefaultReturnMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDBAdapter = new DatabaseAdapter(this);

        // set up UI toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // initialize fab according to application recording state
        mIsRecording = loadRecordingStateFromSharedPref();

        mFileAccessPermission = loadFilePermissionFromSharedPref();
        int fabImageResource = mIsRecording ? R.drawable.ic_stop_white_24dp : R.drawable.ic_menu_listen_white;
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setImageResource(fabImageResource);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRecording(view);
            }
        });

        // get user's default return message from sharedPreference
        this.mDefaultReturnMessage = loadReturnMessageFromSharedPref();

        // set up the hamburger icon to open and close the drawer
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // listen for navigation events
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // initialise the empty view components
        mEmptyView = findViewById(R.id.empty_screen);
        mEmptyViewText = (TextView) findViewById(R.id.no_content_text);

        mActiveNavMenuItemId = R.id.nav_recordings;
        mIsOnActionMode = false;
        mWatchedParentId = 0;

        mNavigationView.getMenu().findItem(mActiveNavMenuItemId).setChecked(true);
        mData = loadMainContent(mActiveNavMenuItemId);

        // set up the recycler view
        mRecyclerView = (CallRecorderRecyclerView) findViewById(R.id.recView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecordingDataAdapter(this, mData);

        mAdapter.setAdapterListener(mAdapterListener);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.hideIfEmpty(mRecyclerView);
        mRecyclerView.showIfEmpty(mEmptyView);
        mRecyclerView.setIsRecording(mIsRecording);

        // enable swipe touch
        SimpleTouchCallback touchCallback = new SimpleTouchCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mAdapter.notifyDataSetChanged();
        mIsNotificationsEnabled = loadIsEnabledSwitchFromSharedPref();
        mRecyclerView.setIsNotificationEnabled(mIsNotificationsEnabled);

        // get user's remind time notification
        this.mRemindTimeSet = loadTimeFromSharedPref();
       // startNotifications();
        if(!mIsRecording && mIsNotificationsEnabled)
            startNotifications();
    }

    public void stopNotifications() {
        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(NotificationService.STOP_ACTION_NOTIF);
        startService(intent);
    }

    public void startNotifications() {
        mRemindTimeSet = loadTimeFromSharedPref();
        Intent intent1 = new Intent(this, NotificationService.class);
        intent1.setAction(NotificationService.START_ACTION_NOTIF);
        intent1.putExtra(NotificationService.TIME_KEY, mRemindTimeSet);
        startService(intent1);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mIsOnActionMode) {
            loadSelectedItems();
            openActionMode();
        }
        verifyStoragePermissions(MainActivity.this);
    }

    // TODO this function
    private void loadSelectedItems() {
        for(ParentRecordingItem p : mSelected) {
            int pos = mData.indexOf(p);
            if (mAdapter != null) {
                mAdapter.toggleSelection(pos);
            }
        }
    }

    private int[] loadTimeFromSharedPref() {
        int time[] = new int[2];
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        time[0] = sharedPreferences.getInt(AppConstants.SHARED_PREFS_NOTIF_TIME_HOUR_KEY, -1);
        time[1] = sharedPreferences.getInt(AppConstants.SHARED_PREFS_NOTIF_TIME_MINUTE_KEY, -1);
        return time;
    }

    private boolean loadRecordingStateFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConstants.SHARED_PREFS_IS_RECORDING_KEY, false);
    }

    private boolean loadIsEnabledSwitchFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConstants.SHARED_PREFS_NOTIF_IS_ENABLED, false);
    }

    private void updateSharedRecordingFilePermission(boolean isPermitted) {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AppConstants.SHARED_PREFS_FILE_PERMISSION, isPermitted);
        editor.commit();
    }

    private boolean loadFilePermissionFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConstants.SHARED_PREFS_FILE_PERMISSION, false);
    }

    private void toggleRecording(View view) {
        if (mIsRecording) {
            Intent intent = new Intent();
            intent.setAction(CallListenerService.STOP_SERVICE_ACTION);
            intent.putExtra(CallListenerService.STOP_SERVICE_BROADCAST_KEY, CallListenerService.STOP_SERVICE_REQUEST);
            sendBroadcast(intent);

            if(mIsNotificationsEnabled) {
                startNotifications();
            }

            mFab.setImageResource(R.drawable.ic_menu_listen_white);
            Snackbar.make(view, "Recording has stopped", Snackbar.LENGTH_LONG).show();
        } else {

            startService(new Intent(this, CallListenerService.class));

            //if(mIsNotificationsEnabled) {
                stopNotifications();
            //}

            mFab.setImageResource(R.drawable.ic_stop_white_24dp);
            Snackbar.make(view, "Recording has started", Snackbar.LENGTH_LONG).show();
        }
        mIsRecording = !mIsRecording;
        mRecyclerView.setIsRecording(mIsRecording);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNewsFileReadyReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsRecording = loadRecordingStateFromSharedPref();
        registerReceiver(mNewsFileReadyReceiver, new IntentFilter(OnFileCreatedForExportReceiver.ACTION_EXPORT_TO_MAIL));

        mIsNotificationsEnabled = loadIsEnabledSwitchFromSharedPref();

        if(mIsNotificationsEnabled) {
            if(!mIsRecording)
                startNotifications();
        }
        else {
            stopNotifications();
        }
        mRecyclerView.setIsNotificationEnabled(mIsNotificationsEnabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null)
            mAdapter.unregisterAdapterListener(mAdapterListener);
    }

    private ArrayList<ParentRecordingItem> extractParentsFromSelectedSparseArray() {
        ArrayList<ParentRecordingItem> toSave = new ArrayList<>();
        if(mAdapter != null) {
            if (mIsOnActionMode) {
                List<Integer> sa = mAdapter.getSelectedItems();
                for (int i = 0; i < sa.size(); i++) {
                    toSave.add((ParentRecordingItem) mData.get(sa.get(i)));
                }
            }
        }
        return toSave;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);

        // Handle navigation view item clicks here.
        mActiveNavMenuItemId = item.getItemId();
        mData = loadMainContent(mActiveNavMenuItemId);
        if (mAdapter != null) {
            mAdapter.update(mData);
        }
        invalidateOptionsMenu();
        return true;
    }

    public void updateDefaultReturnMsg(String msg) {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.SHARED_PREFS_RETURN_MESSAGE_KEY, msg);
        editor.commit();
    }

    public void changeMessagePopUp(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Editor");
        alertDialog.setMessage("Your default message:");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(mDefaultReturnMessage);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mDefaultReturnMessage = input.getText().toString();
                updateDefaultReturnMsg(mDefaultReturnMessage);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });
        alertDialog.show();
    }

    public void startNotificActivity() {
        Intent notif = new Intent(this, NotificationActivity.class);
        startActivity(notif);
    }

    public ArrayList<? extends DatabaseObject> loadMainContent(int menuItemId) {

        String actionBarTitle = "";
        String emptyStateMessage = "";
        ArrayList<? extends DatabaseObject> data = new ArrayList<>();

        switch (menuItemId) {
            case R.id.nav_messages:
                changeMessagePopUp();
            case R.id.nav_recordings:
                data = mDBAdapter.getParentRecords(false);
                emptyStateMessage = "You have no new recordings";
                actionBarTitle = "Recordings";
                break;
            case R.id.nav_notifications:
                data = mDBAdapter.getParentRecords(false);
                emptyStateMessage = "You have no new recordings";
                actionBarTitle = "Recordings";
                startNotificActivity();
                break;
            case R.id.nav_trash:
                data = mDBAdapter.getParentRecords(true);
                emptyStateMessage = "Trash is empty";
                actionBarTitle = "Trash";
                break;
            default:
                break;
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(actionBarTitle);
        }

        // set the watched parent to 0 (not watching children)
        mWatchedParentId = 0;

        // set empty view's title
        setEmptyViewTitle(emptyStateMessage);

        mDrawer.closeDrawer(GravityCompat.START);

        return data;
    }

    private void setEmptyViewTitle(String title) {
        mEmptyViewText.setText(title);
    }

}