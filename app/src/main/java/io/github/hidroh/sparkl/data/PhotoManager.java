package io.github.hidroh.sparkl.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Manager utility to search for remote photos
 */
public interface PhotoManager {
    /**
     * Searches for photos that match given query
     * @param query    search query
     * @param page     page of results if paginated
     * @return true if request is accepted, false otherwise
     */
    boolean search(@NonNull String query, int page);

    /**
     * Subscribes given observer to be notified on async events, ignored if already subscribed
     * @param observer    observer that observes async events
     */
    void subscribe(Observer observer);

    /**
     * Unsubscribes previously subscribed observer
     * @param observer    previously subscribed observer, ignored if never subscribe
     */
    void unsubscribe(Observer observer);

    /**
     * Represents observer that observe photo search events
     */
    abstract class Observer {
        /**
         * Fired when a new search starts
         */
        public void onStart() {}

        /**
         * Fired when a search has been completed
         * @param results    list of matched photos or null if search failed
         */
        public void onComplete(@Nullable List<Photo> results) {}
    }
}
