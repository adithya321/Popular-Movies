package com.adithya321.popularmovies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.adithya321.popularmovies.R;
import com.adithya321.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesAdapter extends ArrayAdapter<Movie> {

    Context context;

    public MoviesAdapter(Context context, List<Movie> movieList) {
        super(context, 0, movieList);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_grid_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.movie_image);
        Picasso.with(context).load(movie.getImagePath()).into(imageView);

        return convertView;
    }
}
