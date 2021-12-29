package com.example.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends BaseAdapter {
    Context mContext;
    List<String> playlists;

    public PlaylistAdapter(Context context, List<String> playlists){
        this.mContext = context;
        this.playlists = playlists;
    }
    @Override
    public int getCount() {
        return playlists.size();
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.playlist_item, null);
        TextView playlistName = view.findViewById(R.id.playlist_name);
        if(playlistName!=null){
            playlistName.setText(playlists.get(position));
        }

        return view;
    }
}
