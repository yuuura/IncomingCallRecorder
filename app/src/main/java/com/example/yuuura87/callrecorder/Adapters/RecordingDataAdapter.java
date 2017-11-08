package com.example.yuuura87.callrecorder.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yuuura87.callrecorder.DataBase.ChildRecordItem;
import com.example.yuuura87.callrecorder.DataBase.DatabaseAdapter;
import com.example.yuuura87.callrecorder.DataBase.DatabaseObject;
import com.example.yuuura87.callrecorder.DataBase.ParentRecordingItem;
import com.example.yuuura87.callrecorder.R;
import com.example.yuuura87.callrecorder.Services.DatabaseService;
import com.example.yuuura87.callrecorder.Services.NotificationService;

import java.util.ArrayList;
import java.util.List;

public class RecordingDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnSwipeListener {

    public static final String TAG = "Adapter";

    private LayoutInflater mInflater;
    private Context mContext;

    private ArrayList<AdapterListener> mListeners = new ArrayList<>();

    private static final int PARENT_VIEW_TYPE = 0;
    private static final int CHILD_VIEW_TYPE = 1;

    private ArrayList<? extends DatabaseObject> mData = new ArrayList<>();

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private boolean mIsInSelectionMode = false;
    private boolean mIsChildExist = false;

    public RecordingDataAdapter(Context context, ArrayList<? extends DatabaseObject> data) {

        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);

        if (data == null) {
            throw new IllegalArgumentException("model data should not be null");
        }

        this.mData = data;
        update(mData);
    }


    public void update(ArrayList<? extends DatabaseObject> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setAdapterListener(AdapterListener listener) {
        mListeners.add(listener);
    }

    public void unregisterAdapterListener(AdapterListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    @Override
    public void onSwipe(int position) {
        deleteItem(position);
    }

    public void deleteItem(int position) {
        Intent intent = new Intent(mContext, DatabaseService.class);
        if(mData.get(position) instanceof ParentRecordingItem) {
            ParentRecordingItem p = (ParentRecordingItem) mData.get(position);
            if (p.getIsTrashed()) {
                intent.setAction(DatabaseService.ACTION_DELETE_PARENT);
                intent.putExtra(DatabaseService.EXTRA_PARENT, p);
            } else {
                intent.setAction(DatabaseService.ACTION_TRASH_PARENT);
                intent.putExtra(DatabaseService.EXTRA_PARENT, p);
            }
        } else {
            ChildRecordItem c = (ChildRecordItem) mData.get(position);
            if (c.getIsTrashed()) {
                intent.setAction(DatabaseService.ACTION_DELETE_CHILD);
                intent.putExtra(DatabaseService.EXTRA_CHILD, c);
            } else {
                intent.setAction(DatabaseService.ACTION_TRASH_CHILD);
                intent.putExtra(DatabaseService.EXTRA_CHILD, c);
            }
        }
        mContext.startService(intent);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == PARENT_VIEW_TYPE) {
            View view = mInflater.inflate(R.layout.card_parent_recording_data_row, parent, false);
            return new ParentRecordingViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.card_child_recording_data_row, parent, false);
            return new ChildRecordingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ParentRecordingViewHolder) {
            ParentRecordingItem current = (ParentRecordingItem) mData.get(position);
            ParentRecordingViewHolder parentHolder = (ParentRecordingViewHolder) holder;
            parentHolder.bindParentRecordingItem(current);
        } else {
            ChildRecordItem current = (ChildRecordItem) mData.get(position);
            ChildRecordingViewHolder childHolder = (ChildRecordingViewHolder) holder;
            childHolder.bindChildRecordingItem(current);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {

        Object obj = mData.get(position);

        if (obj instanceof ParentRecordingItem) {
            return PARENT_VIEW_TYPE;
        } else {
            return CHILD_VIEW_TYPE;
        }
    }

    public void setIsInSelectionMode(boolean isInSelectionMode) {
        this.mIsInSelectionMode = isInSelectionMode;
    }

    public boolean getIsInSelectionMode() {
        return mIsInSelectionMode;
    }

    public boolean isRegisteredAsSelected(int position) {
        return getSelectedItems().contains(position);
    }

    public List<Integer> getSelectedItems() {

        List<Integer> selectedItemsPos = new ArrayList<>(mSelectedItems.size());

        for(int i = 0; i < mSelectedItems.size(); i++) {
            selectedItemsPos.add(mSelectedItems.keyAt(i));
        }
        return selectedItemsPos;
    }

    public void toggleSelection(int position) {

        if(mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {

        setIsInSelectionMode(false);

        List<Integer> selectedItemsPos = getSelectedItems();
        mSelectedItems.clear();

        for (int i = 0; i < selectedItemsPos.size(); i++) {
            notifyItemChanged(selectedItemsPos.get(i));
        }
    }

    public boolean ismIsChildExist() {
        return mIsChildExist;
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    class ParentRecordingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView mName;
        private TextView mNumberOfItems;
        private TextView mDate;
        private ImageView mAvatar;
        private ImageView mIsLocked;
        private CardView mCard;

        private ParentRecordingItem mParentRecording;

        public ParentRecordingViewHolder(View itemView) {
            super(itemView);

            this.mName = (TextView) itemView.findViewById(R.id.list_item_name);
            this.mDate = (TextView) itemView.findViewById(R.id.time);
            this.mNumberOfItems = (TextView) itemView.findViewById(R.id.number_of_items);
            this.mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            this.mIsLocked = (ImageView) itemView.findViewById(R.id.is_locked);
            this.mCard = (CardView) itemView.findViewById(R.id.card_view);

            mCard.setOnClickListener(this);
            mCard.setOnLongClickListener(this);
        }

        public void bindParentRecordingItem(ParentRecordingItem parentRecording) {

            mParentRecording = parentRecording;

            mName.setText(mParentRecording.getRecordName());
            mNumberOfItems.setText(Integer.toString(mParentRecording.getNumActiveChildren()) + " Records");
            if (isRegisteredAsSelected(getAdapterPosition()))
                mAvatar.setImageResource(R.drawable.ic_parent_recording_avatar_selected);
             else
                mAvatar.setImageResource(R.drawable.ic_parent_recording_avatar_unselected);

            mDate.setText(mParentRecording.getStartTime());
            if (mParentRecording.getIsClosed()) {
                mIsLocked.setImageResource(R.drawable.ic_lock_black_24dp);
            } else
                mIsLocked.setImageResource(R.drawable.ic_lock_open_black_24dp);
        }

        @Override
        public boolean onLongClick(View v) {

            if(!getIsInSelectionMode()) {
                setIsInSelectionMode(true);
                for(AdapterListener l : mListeners) {
                    l.onContextMenuState();
                }
            }
            v.callOnClick();
            return true;
        }

        @Override
        public void onClick(View v) {

            if(!getIsInSelectionMode()) {
                for(AdapterListener l : mListeners) {
                    l.onChildrenRequest(mParentRecording, mParentRecording.getIsTrashed());
                }
            } else {
                toggleSelection(getAdapterPosition());
                if (getSelectedItemCount() == 0) {
                    for(AdapterListener l : mListeners) {
                        l.onDeselectingAllItems();
                    }
                }
            }
        }
    }

    class ChildRecordingViewHolder extends RecyclerView.ViewHolder {

     //   private ImageView mAvatar;
        private TextView mCaller;
        private TextView mRoute;
        private TextView mDate;
        private ImageView mBtnCall;
        private RelativeLayout mMessageSection;
        private TextView mMessage;
        private View mDivider;
        private ImageView mBtnMessage;
        private ImageView mBtnDelete;

        private ChildRecordItem mChildRecord;

        public ChildRecordingViewHolder(View itemView) {
            super(itemView);

            this.mCaller = (TextView) itemView.findViewById(R.id.caller);
            this.mRoute = (TextView) itemView.findViewById(R.id.route);
            this.mDate = (TextView) itemView.findViewById(R.id.time);
            this.mBtnDelete = (ImageView) itemView.findViewById(R.id.btn_delete);
            this.mMessageSection = (RelativeLayout) itemView.findViewById(R.id.message_section);
            this.mMessage = (TextView) itemView.findViewById(R.id.message);
            this.mDivider = itemView.findViewById(R.id.message_divider);
            this.mBtnCall = (ImageView) itemView.findViewById(R.id.btn_dial);
            this.mBtnMessage =(ImageView) itemView.findViewById(R.id.btn_message);

            mBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteItem(getAdapterPosition());
                }
            });

            mBtnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mChildRecord.getPhoneNumber()));
                    mContext.startActivity(call);
                }
            });

            mBtnMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + mChildRecord.getPhoneNumber()));
                    mContext.startActivity(smsIntent);
                }
            });
        }

        public void bindChildRecordingItem(ChildRecordItem childRecord) {

            mChildRecord = childRecord;

            mCaller.setText(mChildRecord.getCaller());
            mRoute.setText(mChildRecord.getIsSMS() == true ? "Message " : "Call ");
            if (!mChildRecord.getIsSMS()) {
                mMessageSection.setVisibility(View.GONE);
                mDivider.setVisibility(View.GONE);
            } else {
                mMessageSection.setVisibility(View.VISIBLE);
                mDivider.setVisibility(View.VISIBLE);
                mMessage.setText(mChildRecord.getMessage());
            }
            mDate.setText(mChildRecord.getCallTime());
        }
    }
}

