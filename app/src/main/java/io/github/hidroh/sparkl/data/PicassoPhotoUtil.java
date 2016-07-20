package io.github.hidroh.sparkl.data;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PicassoPhotoUtil implements PhotoUtil {
    @Override
    public void loadPhoto(@NonNull ImageView imageView, @NonNull String url) {
        Picasso.with(imageView.getContext()).load(url).fit().centerCrop().into(imageView);
    }
}
