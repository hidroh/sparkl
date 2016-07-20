package io.github.hidroh.sparkl.data;

import android.os.Parcelable;

/**
 * Represents a remote photo
 */
public interface Photo extends Parcelable {
    /**
     * Gets photo URL
     * @return absolute URL
     */
    String getUrl();

    /**
     * Gets photo title
     * @return title or null
     */
    String getTitle();
}
