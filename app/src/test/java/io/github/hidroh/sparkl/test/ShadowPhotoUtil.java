package io.github.hidroh.sparkl.test;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import io.github.hidroh.sparkl.data.PhotoUtil;

public class ShadowPhotoUtil implements PhotoUtil {
    public String lastLoadedUrl;

    @Override
    public void loadPhoto(@NonNull ImageView imageView, @NonNull String url) {
        lastLoadedUrl = url;
    }

    public void reset() {
        lastLoadedUrl = null;
    }
}
