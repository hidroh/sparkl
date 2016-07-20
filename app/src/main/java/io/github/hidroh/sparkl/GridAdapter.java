package io.github.hidroh.sparkl;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.github.hidroh.sparkl.data.Photo;
import io.github.hidroh.sparkl.data.PhotoManager;
import io.github.hidroh.sparkl.data.PhotoUtil;

class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private static final String STATE_PAGE = "state:page";
    private static final String STATE_QUERY = "state:query";
    private static final String STATE_LIST = "state:list";
    private static final int FIRST_PAGE = 1;
    private static final int VIEW_TYPE_PHOTO = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private final PhotoManager.Observer mObserver = new PhotoManager.Observer() {
        @Override
        public void onComplete(@NonNull List<Photo> results) {
            handleComplete(results);
        }
    };
    private final PhotoManager mPhotoManager;
    private final PhotoUtil mPhotoUtil;
    private final LayoutInflater mInflater;
    private int mPage;
    private String mQuery;
    private ArrayList<Photo> mList = new ArrayList<>();

    GridAdapter(LayoutInflater inflater, PhotoManager photoManager, PhotoUtil photoUtil) {
        mPhotoManager = photoManager;
        mPhotoUtil = photoUtil;
        mInflater = inflater;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mPhotoManager.subscribe(mObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mPhotoManager.unsubscribe(mObserver);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.grid_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_PHOTO:
                holder.mImageView.setContentDescription(mList.get(position).getTitle());
                mPhotoUtil.loadPhoto(holder.mImageView, mList.get(position).getUrl());
                break;
            case VIEW_TYPE_LOADING:
                holder.mImageView.setContentDescription(holder.mImageView.getResources()
                        .getString(R.string.loading));
                holder.mImageView.setImageDrawable(null);
                mPhotoManager.search(mQuery, ++mPage);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_PHOTO;
    }

    /**
     * Sets current query and (asynchronously) refreshes grid data to match it
     * @param query    search query
     */
    void setQuery(@NonNull String query) {
        mQuery = query;
        mPage = FIRST_PAGE;
        mPhotoManager.search(query, mPage);
    }

    /**
     * Gets previously set query
     * @return  search query
     */
    String getQuery() {
        return mQuery;
    }

    /**
     * Saves adapter state into given bundle
     * @param outState    out bundle
     */
    void saveState(@NonNull Bundle outState) {
        outState.putInt(STATE_PAGE, mPage);
        outState.putString(STATE_QUERY, mQuery);
        outState.putParcelableArrayList(STATE_LIST, mList);
    }

    /**
     * Restores previously saved state from bundle to this adapter
     * @param savedState    previously saved state
     */
    void restoreState(@NonNull Bundle savedState) {
        mPage = savedState.getInt(STATE_PAGE);
        mQuery = savedState.getString(STATE_QUERY);
        mList = savedState.getParcelableArrayList(STATE_LIST);
    }

    private void handleComplete(List<Photo> results) {
        if (mPage == FIRST_PAGE) { // 1st load: clear existing, append results and loading placeholder if non-empty
            mList.clear();
            if (!results.isEmpty()) {
                mList.addAll(results);
                mList.add(null);
            }
            notifyDataSetChanged();
        } else if (!results.isEmpty()) { // nth load: append results, move loading placeholder
            int index = mList.size()-1;
            mList.remove(index);
            mList.addAll(results);
            mList.add(null);
            notifyItemChanged(index);
            notifyItemRangeInserted(index+1, results.size());
        } else if (!mList.isEmpty()) { // nth load, empty results: remove loading placeholder
            int index = mList.size()-1;
            mList.remove(index);
            notifyItemRemoved(index);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
        }
    }
}
