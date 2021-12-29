package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class SongAdapter extends BaseAdapter {
    Context mContext;
    String[] songs;
    public SongAdapter(Context context, String[] songs){
        this.mContext = context;
        this.songs = songs;
    }
    @Override
    public int getCount() {
        return songs.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.song_item, null);
        TextView songName = view.findViewById(R.id.song_name);
        if(songName!=null){
            songName.setText(songs[position]);
        }

        return view;
    }
}