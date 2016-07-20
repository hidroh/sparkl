package io.github.hidroh.sparkl;

import android.support.annotation.NonNull;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

import io.github.hidroh.sparkl.data.PhotoManager;
import io.github.hidroh.sparkl.data.PhotoUtil;
import io.github.hidroh.sparkl.test.ShadowPhotoManager;
import io.github.hidroh.sparkl.test.ShadowPhotoUtil;

public class TestApplication extends Application implements TestLifecycleApplication {
    public final ShadowPhotoManager photoManager = new ShadowPhotoManager();
    public final ShadowPhotoUtil photoUtil = new ShadowPhotoUtil();

    @NonNull
    @Override
    PhotoManager createPhotoManager() {
        return photoManager;
    }

    @NonNull
    @Override
    PhotoUtil createPhotoUtil() {
        return photoUtil;
    }

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {

    }
}
