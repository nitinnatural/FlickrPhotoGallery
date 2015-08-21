package flikrgallery.androidyug.www.flikrgallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IAMONE on 8/17/2015.
 */
public class FlickrGalleryFragment extends Fragment {
    private static final String LOG_TAG = "flikr_gallery_fragment";

    private GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbDownloader<ImageView> mThumbDownloaderThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        mThumbDownloaderThread = new ThumbDownloader<ImageView>(new Handler());
        mThumbDownloaderThread.setListener(new ThumbDownloader.Listener<ImageView>(){
            @Override
            public void onThumbDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()){
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });

        mThumbDownloaderThread.start();
        mThumbDownloaderThread.getLooper();
        Log.i(LOG_TAG, "background thread started");
    }

    public void updateItems(){
        new FetchItemTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_gallery_flickr, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);

        //setupAdapter();

        return v;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbDownloaderThread.quit();
        mThumbDownloaderThread.clearQueue();
        Log.i(LOG_TAG, "background thread destroyed");
    }

    void setupAdapter(){
        if(getActivity() == null || mGridView == null)
            return;

        if(mItems != null){
            mGridView.setAdapter(new GalleryAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    // Inner Class
    private class FetchItemTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            Activity activity = getActivity();
            if(activity == null){
                return new ArrayList<GalleryItem>();
            }

            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getString(FlickrFetcher.PREF_SEARCH_QUERY, null);
            if(query != null){
                return new FlickrFetcher().search(query);
            } else {
                return new FlickrFetcher().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    // gallery adapter
    private class GalleryAdapter extends ArrayAdapter<GalleryItem>{

        public GalleryAdapter(List<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }


            ImageView imageView = (ImageView) convertView.findViewById(R.id.ivGalleryItem);
            GalleryItem item = getItem(position);
            mThumbDownloaderThread.queueThumbnail(imageView, item.getUrl());
            return convertView;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_flickr_gallery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetcher.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
