package com.adithya321.popularmovies.model;

public class Movie {
    String title;
    int image;

    public Movie(String title, int image) {
        this.title = title;
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getImage() {
        return image;
    }
}
