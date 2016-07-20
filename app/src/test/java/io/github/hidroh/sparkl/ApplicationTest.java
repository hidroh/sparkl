package io.github.hidroh.sparkl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import io.github.hidroh.sparkl.data.PicassoPhotoUtil;
import io.github.hidroh.sparkl.data.flickr.FlickrPhotoManager;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ApplicationTest {
    @Test
    public void testGetPhotoManager() {
        assertThat(new Application().createPhotoManager())
                .isInstanceOf(FlickrPhotoManager.class);
    }

    @Test
    public void testGetPhotoUtil() {
        assertThat(new Application().createPhotoUtil())
                .isInstanceOf(PicassoPhotoUtil.class);
    }
}
