package io.github.hidroh.sparkl.data.flickr;

import android.os.Parcel;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.hidroh.sparkl.BuildConfig;
import io.github.hidroh.sparkl.data.Photo;
import io.github.hidroh.sparkl.data.PhotoManager;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * {@link PhotoManager} that relies on Flickr for data source
 */
public class FlickrPhotoManager implements PhotoManager {

    private final Set<Observer> mObservers = new HashSet<>();
    private final RestService mRestService;
    private Call<SearchResults> mCall;

    public static RestService createRestService() {
        return new Retrofit.Builder()
                .baseUrl(RestService.BASE_URL)
                .callFactory(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RestService.class);
    }

    public FlickrPhotoManager(RestService restService) {
        mRestService = restService;
    }

    @Override
    public void search(@NonNull String query, int page) {
        if (mCall != null) { // only allow 1 call at a time
            return;
        }
        for (Observer observer : mObservers) {
            observer.onStart();
        }
        mCall = mRestService.search(query, page);
        mCall.enqueue(new Callback<SearchResults>() {
            @Override
            public void onResponse(Call<SearchResults> call, Response<SearchResults> response) {
                mCall = null;
                SearchResults searchResults = response.body();
                for (Observer observer : mObservers) {
                    observer.onComplete(searchResults != null ?
                            searchResults.getPhotos() : new ArrayList<Photo>());
                }
            }

            @Override
            public void onFailure(Call<SearchResults> call, Throwable t) {
                mCall = null;
                for (Observer observer : mObservers) {
                    observer.onComplete(new ArrayList<Photo>());
                }
            }
        });
    }

    @Override
    public void subscribe(Observer observer) {
        mObservers.add(observer);
    }

    @Override
    public void unsubscribe(Observer observer) {
        if (!mObservers.remove(observer)) {
            return;
        }
        if (mObservers.isEmpty() && mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }

    interface RestService {
        String BASE_URL = "https://api.flickr.com/";
        String PATH_SEARCH = "services/rest/?method=flickr.photos.search&" +
                "api_key="+ BuildConfig.FLICKR_API_KEY + "&" +
                "format=json&" +
                "nojsoncallback=1&" +
                "media=photos&" +
                "extras=url_n&" +
                "per_page=50";

        @GET(PATH_SEARCH)
        Call<SearchResults> search(@Query("text") String query, @Query("page") int page);
    }

    static class SearchResults {
        @Keep PhotoList photos;

        List<Photo> getPhotos() {
            if (photos == null) {
                return new ArrayList<>();
            }
            if (photos.photo == null) {
                return new ArrayList<>();
            }
            ArrayList<Photo> list = new ArrayList<>();
            Collections.addAll(list, photos.photo);
            return list;
        }
    }

    static class PhotoList {
        @Keep FlickrPhoto[] photo;
    }

    static class FlickrPhoto implements Photo {
        public static final Creator<FlickrPhoto> CREATOR = new Creator<FlickrPhoto>() {
            @Override
            public FlickrPhoto createFromParcel(Parcel parcel) {
                return new FlickrPhoto(parcel);
            }

            @Override
            public FlickrPhoto[] newArray(int size) {
                return new FlickrPhoto[size];
            }
        };

        @Keep String url_n;
        @Keep String title;

        private FlickrPhoto(Parcel parcel) {
            url_n = parcel.readString();
            title = parcel.readString();
        }

        @Override
        public String getUrl() {
            return url_n;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(url_n);
            parcel.writeString(title);
        }
    }
}

