package io.github.hidroh.sparkl.test;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.hidroh.sparkl.data.Photo;
import io.github.hidroh.sparkl.data.PhotoManager;

public class ShadowPhotoManager implements PhotoManager {
    public final Set<Observer> set = new HashSet<>();
    public String lastQuery;
    public int lastPage;

    @Override
    public void search(@NonNull String query, int page) {
        lastQuery = query;
        lastPage = page;
    }

    @Override
    public void subscribe(Observer observer) {
        set.add(observer);
    }

    @Override
    public void unsubscribe(Observer observer) {
        set.remove(observer);
    }

    public void notifyStart() {
        for (Observer observer : set) {
            observer.onStart();
        }
    }

    public void notifyComplete(List<Photo> results) {
        for (Observer observer : set) {
            observer.onComplete(results);
        }
    }

    public void reset() {
        set.clear();
        lastQuery = null;
        lastPage = 0;
    }
}
