package com.seventhmoon.jamnow;

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

public class VoiceSearchParams {
    public final String query;
    public boolean isAny;
    public boolean isUnstructured;
    public boolean isGenreFocus;
    public boolean isArtistFocus;
    public boolean isAlbumFocus;
    public boolean isSongFocus;
    public String genre;
    public String artist;
    public String album;
    public String song;

    /**
     * Creates a simple object describing the search criteria from the query and extras.
     * @param query the query parameter from a voice search
     * @param extras the extras parameter from a voice search
     */
    public VoiceSearchParams(String query, Bundle extras) {
        this.query = query;

        if (TextUtils.isEmpty(query)) {
            // A generic search like "Play music" sends an empty query
            isAny = true;
        } else {
            if (extras == null) {
                isUnstructured = true;
            } else {
                String genreKey;
                if (Build.VERSION.SDK_INT >= 21) {
                    genreKey = MediaStore.EXTRA_MEDIA_GENRE;
                } else {
                    genreKey = "android.intent.extra.genre";
                }

                String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
                if (TextUtils.equals(mediaFocus, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)) {
                    // for a Genre focused search, only genre is set:
                    isGenreFocus = true;
                    genre = extras.getString(genreKey);
                    if (TextUtils.isEmpty(genre)) {
                        // Because of a bug on the platform, genre is only sent as a query, not as
                        // the semantic-aware extras. This check makes it future-proof when the
                        // bug is fixed.
                        genre = query;
                    }
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                    // for an Artist focused search, both artist and genre are set:
                    isArtistFocus = true;
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                    // for an Album focused search, album, artist and genre are set:
                    isAlbumFocus = true;
                    album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)) {
                    // for a Song focused search, title, album, artist and genre are set:
                    isSongFocus = true;
                    song = extras.getString(MediaStore.EXTRA_MEDIA_TITLE);
                    album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else {
                    // If we don't know the focus, we treat it is an unstructured query:
                    isUnstructured = true;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "query=" + query
                + " isAny=" + isAny
                + " isUnstructured=" + isUnstructured
                + " isGenreFocus=" + isGenreFocus
                + " isArtistFocus=" + isArtistFocus
                + " isAlbumFocus=" + isAlbumFocus
                + " isSongFocus=" + isSongFocus
                + " genre=" + genre
                + " artist=" + artist
                + " album=" + album
                + " song=" + song;
    }
}