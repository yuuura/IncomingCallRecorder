package com.example.yuuura87.callrecorder.DataBase;

import android.os.Parcel;
import android.os.Parcelable;

public class ParentRecordingItem implements DatabaseObject, Parcelable {

    private long mID;
    private String mRecordName;
    private String mStartTime;
    private long mEndTime;
    private boolean mIsClosed;
    private boolean mIsTrashed;
    private int mNumActiveChildren;
    private int mNumTrashedChildren;


    public ParentRecordingItem() {}

    public  ParentRecordingItem(Parcel input) {
        mID = input.readLong();
        mRecordName = input.readString();
        mStartTime = input.readString();
        mEndTime = input.readLong();
        mIsClosed = input.readInt() != 0;
        mIsTrashed = input.readInt() != 0;
        mNumActiveChildren = input.readInt();
        mNumTrashedChildren = input.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mID);
        dest.writeString(mRecordName);
        dest.writeString(mStartTime);
        dest.writeLong(mEndTime);
        dest.writeInt(mIsClosed ? 1 : 0);
        dest.writeInt(mIsTrashed ? 1 : 0);
        dest.writeInt(mNumActiveChildren);
        dest.writeInt(mNumTrashedChildren);
    }

    public static final Parcelable.Creator<ParentRecordingItem> CREATOR = new Parcelable.Creator<ParentRecordingItem>() {

        @Override
        public ParentRecordingItem createFromParcel(Parcel source) {
            return new ParentRecordingItem(source);
        }

        @Override
        public ParentRecordingItem[] newArray(int size) {
            return new ParentRecordingItem[size];
        }
    };

    public void setID(long id) {
        this.mID = id;
    }

    public long getID() {
        return mID;
    }

    public String getRecordName() {
        return mRecordName;
    }

    public void setRecordName(String mRecordName) {
        this.mRecordName = mRecordName;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        this.mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
    }

    public boolean getIsClosed() {
        return mIsClosed;
    }

    public void setIsClosed(boolean isClosed) {
        this.mIsClosed = isClosed;
    }

    public boolean getIsTrashed() { return mIsTrashed; }

    public void setIsTrashed(boolean trashed) {
        this.mIsTrashed = trashed;
    }

    public int getNumActiveChildren() {
        return mNumActiveChildren;
    }

    public void setNumActiveChildren(int numActiveChildren) {
        this.mNumActiveChildren = numActiveChildren;
    }

    public int getNumTrashedChildren() {
        return mNumTrashedChildren;
    }

    public void setNumTrashedChildren(int numTrashedChildren) {
        this.mNumTrashedChildren = numTrashedChildren;
    }

    @Override
    public boolean equals(Object o) {
        boolean res = false;
        if (o instanceof ParentRecordingItem) {
            ParentRecordingItem other = (ParentRecordingItem) o;
            res = (this.getID() == other.getID());
        }
        return res;
    }

    @Override
    public String toString() {
        return "ParentRecordingItem{" +
                "mEndTime=" + mEndTime +
                ", mID=" + mID +
                ", mRecordName='" + mRecordName + '\'' +
                ", mStartTime=" + mStartTime +
                ", mIsClosed=" + mIsClosed +
                ", mIsTrashed=" + mIsTrashed +
                ", mNumActiveChildren=" + mNumActiveChildren +
                ", mNumTrashedChildren=" + mNumTrashedChildren +
                '}';
    }
}
