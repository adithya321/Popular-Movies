package com.adithya321.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.adithya321.popularmovies.adapters.MoviesAdapter;
import com.adithya321.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An activity representing a grid of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a grid of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the grid of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;
    private GridView gridView;

    private String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private String MOVIES_URL;

    private Menu menu;
    private ArrayList<Movie> movieList;
    private int checkedMenuId;
    private Bundle savedInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        gridView = (GridView) findViewById(R.id.movie_grid);
        assert gridView != null;
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(MovieDetailFragment.ARG_ITEM_ID, view.toString());
                    MovieDetailFragment fragment = new MovieDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);
                    intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, view.toString());

                    startActivity(intent);
                }
            }
        });

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (savedInstanceState != null) {
            movieList = savedInstanceState.getParcelableArrayList("movieList");
            setGridViewAdapter();

            savedInstance = savedInstanceState;
        } else {
            MOVIES_URL = MOVIES_BASE_URL + "popular?";
            new FetchMoviesTask().execute(MOVIES_URL);
        }
    }

    private void setGridViewAdapter() {
        MoviesAdapter moviesAdapter = new MoviesAdapter(getApplicationContext(), movieList);
        gridView.setAdapter(moviesAdapter);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... strings) {

            HttpURLConnection urlConnection;
            BufferedReader reader;
            String moviesJsonStr;

            try {
                Uri builtUri = Uri.parse(strings[0]).buildUpon()
                        .appendQueryParameter("api_key", getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line + "\n");

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();

                try {
                    return getMovieDataFromJson(moviesJsonStr, 12);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }

            return new Movie[0];
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            super.onPostExecute(movies);

            movieList = new ArrayList<>(Arrays.asList(movies));
            setGridViewAdapter();
        }

        private Movie[] getMovieDataFromJson(String moviesJsonStr, int numMovies) throws JSONException {

            final String TMDB_LIST = "results";
            final String TMDB_TITLE = "title";
            final String TMDB_POSTER = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

            Movie[] resultMovies = new Movie[numMovies];

            for (int i = 0; i < numMovies; i++) {
                JSONObject movieDetails = moviesArray.getJSONObject(i);

                String title = movieDetails.getString(TMDB_TITLE);
                String image = movieDetails.getString(TMDB_POSTER);

                resultMovies[i] = new Movie();
                resultMovies[i].setTitle(title);
                resultMovies[i].setImagePath("http://image.tmdb.org/t/p/w185" + image);
            }

            return resultMovies;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        this.menu = menu;

        if (savedInstance != null)
            setMenuItemsChecked(savedInstance.getInt("sort"));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        checkedMenuId = item.getItemId();
        setMenuItemsChecked(item.getItemId());

        switch (item.getItemId()) {
            case R.id.action_now:
                MOVIES_URL = MOVIES_BASE_URL + "now_playing?";
                break;
            case R.id.action_popular:
                MOVIES_URL = MOVIES_BASE_URL + "popular?";
                break;
            case R.id.action_top:
                MOVIES_URL = MOVIES_BASE_URL + "top_rated?";
                break;
            case R.id.action_upcoming:
                MOVIES_URL = MOVIES_BASE_URL + "upcoming?";
                break;
        }

        new FetchMoviesTask().execute(MOVIES_URL);

        return super.onOptionsItemSelected(item);
    }

    private void setMenuItemsChecked(int id) {
        int items[] = {R.id.action_now, R.id.action_popular, R.id.action_top, R.id.action_upcoming};

        for (int item : items) {
            if (item == id) menu.findItem(item).setChecked(true);
            else menu.findItem(item).setChecked(false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movieList", movieList);
        outState.putInt("sort", checkedMenuId);
        super.onSaveInstanceState(outState);
    }
}
