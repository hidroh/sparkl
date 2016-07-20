package io.github.hidroh.sparkl.data.flickr;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import java.io.IOException;

import io.github.hidroh.sparkl.data.Photo;
import io.github.hidroh.sparkl.data.PhotoManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class FlickrPhotoManagerTest {
    private final FlickrPhotoManager.RestService restService = mock(FlickrPhotoManager.RestService.class);
    @SuppressWarnings("unchecked")
    private final Call<FlickrPhotoManager.SearchResults> call = mock(Call.class);
    private FlickrPhotoManager photoManager;
    @Captor ArgumentCaptor<Callback<FlickrPhotoManager.SearchResults>> callbackCaptor;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(restService.search(anyString(), anyInt())).thenReturn(call);
        photoManager = new FlickrPhotoManager(restService);
    }

    @Test
    public void testComplete() {
        PhotoManager.Observer observer = mock(PhotoManager.Observer.class);
        photoManager.subscribe(observer);
        photoManager.search("query", 1);
        verify(observer).onStart();
        verify(restService).search(eq("query"), eq(1));
        verify(call).enqueue(callbackCaptor.capture());
        callbackCaptor.getValue().onResponse(call, Response.success(new FlickrPhotoManager.SearchResults()));
        verify(observer).onComplete(anyListOf(Photo.class));
        photoManager.unsubscribe(observer);
    }

    @Test
    public void testNullResults() {
        PhotoManager.Observer observer = mock(PhotoManager.Observer.class);
        photoManager.subscribe(observer);
        photoManager.search("query", 1);
        verify(observer).onStart();
        verify(restService).search(eq("query"), eq(1));
        verify(call).enqueue(callbackCaptor.capture());
        callbackCaptor.getValue().onResponse(call, Response.<FlickrPhotoManager.SearchResults>success(null));
        verify(observer).onComplete(anyListOf(Photo.class));
        photoManager.unsubscribe(observer);
    }

    @Test
    public void testError() {
        PhotoManager.Observer observer = mock(PhotoManager.Observer.class);
        photoManager.subscribe(observer);
        photoManager.search("query", 1);
        verify(restService).search(eq("query"), eq(1));
        verify(call).enqueue(callbackCaptor.capture());
        callbackCaptor.getValue().onFailure(call, null);
        verify(observer).onComplete(anyListOf(Photo.class));
        photoManager.unsubscribe(observer);
    }

    @Test
    public void testNoConcurrentSearch() {
        photoManager.search("query", 1);
        photoManager.search("query", 1);
        verify(restService).search(eq("query"), eq(1));
        verify(call).enqueue(callbackCaptor.capture());
        callbackCaptor.getValue().onFailure(call, null);
        photoManager.search("query", 1);
        verify(restService, times(2)).search(eq("query"), eq(1));
    }

    @Test
    public void testPrematureUnsubscribe() {
        PhotoManager.Observer observer = mock(PhotoManager.Observer.class);
        photoManager.subscribe(observer);
        photoManager.search("query", 1);
        photoManager.unsubscribe(observer);
        verify(call).cancel();
    }

    @Test
    public void testInvalidUnsubscribe() {
        photoManager.search("query", 1);
        photoManager.unsubscribe(mock(PhotoManager.Observer.class));
        verify(call, never()).cancel();
    }

    @Test
    public void testMultipleObservers() {
        PhotoManager.Observer observer1 = mock(PhotoManager.Observer.class);
        PhotoManager.Observer observer2 = mock(PhotoManager.Observer.class);
        photoManager.subscribe(observer1);
        photoManager.subscribe(observer2);
        photoManager.search("query", 1);
        photoManager.unsubscribe(observer1);
        verify(call, never()).cancel();
        photoManager.unsubscribe(observer2);
        verify(call).cancel();
    }

    @Test
    public void testGetPhotos() {
        FlickrPhotoManager.SearchResults searchResults = new FlickrPhotoManager.SearchResults();
        assertThat(searchResults.getPhotos()).isEmpty();

        searchResults.photos = new FlickrPhotoManager.PhotoList();
        assertThat(searchResults.getPhotos()).isEmpty();

        searchResults.photos.photo = new FlickrPhotoManager.FlickrPhoto[]{
                FlickrPhotoManager.FlickrPhoto.CREATOR.createFromParcel(Parcel.obtain())};
        assertThat(searchResults.getPhotos()).hasSize(1);
    }

    @Test
    public void testParcelable() {
        assertThat(FlickrPhotoManager.FlickrPhoto.CREATOR.newArray(5)).hasSize(5);

        Parcel readParcel = Parcel.obtain();
        readParcel.writeString("http://example.com/image.jpg");
        readParcel.writeString("Title");
        readParcel.setDataPosition(0);
        FlickrPhotoManager.FlickrPhoto photo = FlickrPhotoManager.FlickrPhoto.CREATOR.createFromParcel(readParcel);
        assertThat(photo.getUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(photo.getTitle()).isEqualTo("Title");

        Parcel writeParcel = Parcel.obtain();
        photo.writeToParcel(writeParcel, 0);
        writeParcel.setDataPosition(0);
        assertThat(writeParcel.readString()).isEqualTo("http://example.com/image.jpg");
        assertThat(writeParcel.readString()).isEqualTo("Title");
    }
}
