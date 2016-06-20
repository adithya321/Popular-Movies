package com.adithya321.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    String title;
    String imagePath;

    public Movie() {

    }

    public Movie(String title, String imagePath) {
        this.title = title;
        this.imagePath = imagePath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(imagePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
