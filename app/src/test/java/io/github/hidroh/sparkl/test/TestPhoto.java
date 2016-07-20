package io.github.hidroh.sparkl.test;

import android.annotation.SuppressLint;
import android.os.Parcel;

import io.github.hidroh.sparkl.data.Photo;

@SuppressLint("ParcelCreator")
public class TestPhoto implements Photo {
    private final String url;
    private final String title;

    public TestPhoto(String url, String title) {
        this.url = url;
        this.title = title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
