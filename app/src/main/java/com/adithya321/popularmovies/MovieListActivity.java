/*
 * Popular Movies
 * Copyright (C) 2017 Adithya J
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.adithya321.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

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

                ImageView imageView = view.findViewById(R.id.movie_image);

                Bundle arguments = new Bundle();
                if (mTwoPane) {
                    arguments.putParcelable(MovieDetailFragment.ARG_ITEM_ID,
                            (Parcelable) imageView.getTag());
                    MovieDetailFragment fragment = new MovieDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment)
                            .commit();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MovieDetailActivity.class);
                    intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, (Parcelable) imageView.getTag());
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

    @SuppressLint("StaticFieldLeak")
    private class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

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
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();

                try {
                    return getMovieDataFromJson(moviesJsonStr);
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

        private Movie[] getMovieDataFromJson(String moviesJsonStr) throws JSONException {

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray("results");

            Movie[] resultMovies = new Movie[12];

            for (int i = 0; i < 12; i++) {
                JSONObject movieDetails = moviesArray.getJSONObject(i);

                resultMovies[i] = new Movie();
                resultMovies[i].setId(movieDetails.getInt("id"));
                resultMovies[i].setTitle(movieDetails.getString("title"));
                resultMovies[i].setImagePath("http://image.tmdb.org/t/p/w185" +
                        movieDetails.getString("poster_path"));
                resultMovies[i].setPlot(movieDetails.getString("overview"));
                resultMovies[i].setRating(movieDetails.getDouble("vote_average"));
                resultMovies[i].setReleaseDate(movieDetails.getString("release_date"));
            }

            return resultMovies;
        }
    }
}
