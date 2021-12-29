package com.rcfin.messenger.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    public String uuid;
    public String name;
    public String email;
    public String profileUrl;
    public Bitmap bitmap;
    public boolean isOnline;

    public User() {
    }

    public User(String uuid, String name, String email, String profileUrl) {
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.profileUrl = profileUrl;
    }

    protected User(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        email = in.readString();
        profileUrl = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        isOnline = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(profileUrl);
        dest.writeParcelable(bitmap, flags);
        dest.writeByte((byte) (isOnline ? 1 : 0));
    }
}
