package io.github.hidroh.sparkl.data;

import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Utility class to load remote photos into views
 */
public interface PhotoUtil {
    /**
     * Loads remote photo at given URL into given {@link ImageView}
     * @param imageView    view that will display photo
     * @param url          remote photo absolute URL
     */
    void loadPhoto(@NonNull ImageView imageView, @NonNull String url);
}
