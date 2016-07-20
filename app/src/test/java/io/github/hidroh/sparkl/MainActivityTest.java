package io.github.hidroh.sparkl;

import android.app.SearchManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;

import io.github.hidroh.sparkl.data.Photo;
import io.github.hidroh.sparkl.test.ShadowPhotoManager;
import io.github.hidroh.sparkl.test.ShadowPhotoUtil;
import io.github.hidroh.sparkl.test.TestPhoto;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class MainActivityTest {
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private ShadowPhotoManager photoManager;
    private ShadowPhotoUtil photoUtil;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    @Before
    public void setUp() {
        photoManager = ((TestApplication) RuntimeEnvironment.application).photoManager;
        photoUtil = ((TestApplication) RuntimeEnvironment.application).photoUtil;
        photoManager.reset();
        photoUtil.reset();
        controller = Robolectric.buildActivity(MainActivity.class);
        activity = controller.withIntent(createSearchIntent())
                .create()
                .start()
                .resume()
                .visible()
                .get();
        recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        adapter = recyclerView.getAdapter();
    }

    @Test
    public void testWithIntent() {
        assertThat(photoManager.lastQuery).isEqualTo("query");
        assertThat(photoManager.lastPage).isEqualTo(1);
        assertThat(activity).hasTitle("query");
        assertThat(activity.findViewById(android.R.id.empty)).isNotVisible();
    }

    @Test
    public void testNewIntent() {
        controller.pause().stop().destroy();
        photoManager.reset();

        controller = Robolectric.buildActivity(MainActivity.class);
        activity = controller.create().start().resume().visible().get();
        assertThat(photoManager.lastQuery).isNullOrEmpty();
        assertThat(photoManager.lastPage).isEqualTo(0);
        assertThat(activity).hasTitle(R.string.app_name);
        assertThat(activity.findViewById(android.R.id.empty)).isVisible();

        controller.newIntent(createSearchIntent());
        assertThat(photoManager.lastQuery).isEqualTo("query");
        assertThat(photoManager.lastPage).isEqualTo(1);
        assertThat(activity).hasTitle("query");
        assertThat(activity.findViewById(android.R.id.empty)).isNotVisible();
    }

    @Test
    public void testProgress() {
        photoManager.notifyStart();
        assertThat(activity.findViewById(android.R.id.progress)).isVisible();
        photoManager.notifyComplete(new ArrayList<Photo>());
        assertThat(activity.findViewById(android.R.id.progress)).isNotVisible();
    }

    @Test
    public void testEmptyResults() {
        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>());
        assertThat(adapter.getItemCount()).isEqualTo(0);
    }

    @Test
    public void testGridItemBinding() {
        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>() {{
            add(new TestPhoto("http://example.com/kitten.jpg", "Kitten"));
        }});
        assertThat(adapter.getItemCount()).isEqualTo(2); // 1 item + 1 placeholder
        RecyclerView.ViewHolder viewHolder = createBindHolder(0);
        assertThat(photoUtil.lastLoadedUrl).isEqualTo("http://example.com/kitten.jpg");
        assertThat((ImageView) viewHolder.itemView).hasContentDescription("Kitten");
    }

    @Test
    public void testLoadingMore() {
        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>() {{
            add(new TestPhoto("http://example.com/kitten.jpg", "Kitten"));
        }});
        RecyclerView.ViewHolder viewHolder = createBindHolder(1);
        assertThat((ImageView) viewHolder.itemView)
                .hasContentDescription(R.string.loading)
                .hasDrawable(null);
        assertThat(photoManager.lastQuery).isEqualTo("query");
        assertThat(photoManager.lastPage).isEqualTo(2);
    }

    @Test
    public void testEmptyLoadingMore() {
        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>() {{
            add(new TestPhoto("http://example.com/kitten.jpg", "Kitten"));
        }});

        RecyclerView.ViewHolder viewHolder = createBindHolder(1);
        assertThat((ImageView) viewHolder.itemView)
                .hasContentDescription(R.string.loading)
                .hasDrawable(null);
        assertThat(photoManager.lastQuery).isEqualTo("query");
        assertThat(photoManager.lastPage).isEqualTo(2);

        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>() {{
            add(new TestPhoto("http://example.com/kitten.jpg", "Kitten"));
        }});
        assertThat(adapter.getItemCount()).isEqualTo(3); // 2 items + 1 placeholder

        createBindHolder(2);
        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>());
        assertThat(adapter.getItemCount()).isEqualTo(2); // 2 items
    }

    @Test
    public void testStateRestoration() {
        photoManager.notifyStart();
        photoManager.notifyComplete(new ArrayList<Photo>() {{
            add(new TestPhoto("http://example.com/kitten.jpg", "Kitten"));
        }});
        assertThat(adapter.getItemCount()).isEqualTo(2); // 1 item + 1 placeholder
        assertThat(activity.findViewById(android.R.id.empty)).isNotVisible();
        photoManager.reset();
        shadowOf(activity).recreate();
        assertThat(photoManager.lastQuery).isNullOrEmpty(); // should not make more query
        assertThat(adapter.getItemCount()).isEqualTo(2); // 1 item + 1 placeholder
        assertThat(activity.findViewById(android.R.id.empty)).isNotVisible();
    }

    @NonNull
    private RecyclerView.ViewHolder createBindHolder(int position) {
        RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(recyclerView,
                adapter.getItemViewType(position));
        //noinspection unchecked
        adapter.bindViewHolder(viewHolder, position);
        return viewHolder;
    }

    private Intent createSearchIntent() {
        return new Intent(Intent.ACTION_SEARCH).putExtra(SearchManager.QUERY, "query");
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }
}
