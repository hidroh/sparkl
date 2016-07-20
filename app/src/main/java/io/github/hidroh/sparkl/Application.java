package io.github.hidroh.sparkl;

import android.support.annotation.NonNull;

import io.github.hidroh.sparkl.data.PhotoManager;
import io.github.hidroh.sparkl.data.PhotoUtil;
import io.github.hidroh.sparkl.data.PicassoPhotoUtil;
import io.github.hidroh.sparkl.data.flickr.FlickrPhotoManager;

/**
 * Slight extension of {@link android.app.Application}
 * that also acts as a dependency injection container
 */
public class Application extends android.app.Application {
    /**
     * Instantiates a new instance of {@link PhotoManager}
     * @return  photo manager instance
     */
    @NonNull
    PhotoManager createPhotoManager() {
        return new FlickrPhotoManager(FlickrPhotoManager.createRestService());
    }

    /**
     * Instantiates a new instance of {@link PhotoUtil}
     * @return  photo util instance
     */
    @NonNull
    PhotoUtil createPhotoUtil() {
        return new PicassoPhotoUtil();
    }
}
