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

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adithya321.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            Movie movie = getArguments().getParcelable(ARG_ITEM_ID);
            assert movie != null;

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(movie.getTitle());
            }

            ((TextView) rootView.findViewById(R.id.movie_title)).setText(movie.getTitle());
            Picasso.with(getActivity()).load(movie.getImagePath())
                    .into((ImageView) rootView.findViewById(R.id.movie_image));
            ((TextView) rootView.findViewById(R.id.movie_plot)).setText(movie.getPlot());
            ((TextView) rootView.findViewById(R.id.movie_rating)).setText(getString(R.string.movie_detail_rating, movie.getRating()));
            ((TextView) rootView.findViewById(R.id.movie_release)).setText(movie.getReleaseDate());
        }

        return rootView;
    }
}
