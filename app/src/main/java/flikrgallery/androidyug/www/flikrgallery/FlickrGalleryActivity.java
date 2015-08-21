package flikrgallery.androidyug.www.flikrgallery;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class FlickrGalleryActivity extends SingleFragmentActivity {

    private static final String LOG_TAG = "FlickrGalleryFragment";


    @Override
    protected void onNewIntent(Intent intent) {
        FlickrGalleryFragment fragment = (FlickrGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.container);

        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(LOG_TAG, "Receive a new search query: " + query);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetcher.PREF_SEARCH_QUERY, query)
                    .commit();
        }

        fragment.updateItems();
    }


    @Override
    protected Fragment createFragment() {
        return new FlickrGalleryFragment();
    }
}
