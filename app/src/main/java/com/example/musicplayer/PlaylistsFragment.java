package com.example.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment {

    RelativeLayout createPlaylistRow;
    private DBHandler dbHandler;
    List<String> playlist = new ArrayList<>();
    ArrayList<String> playlist_songs = new ArrayList<>();
    ListView playlist_view;

    private static int position = 0;
    private ArrayList<File> allSongs = null;

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d("TAG1", "After activity end");
                    Toast.makeText(getActivity().getBaseContext(), "Playlist created", Toast.LENGTH_SHORT).show();
                    // Reload current fragment
                    dbHandler = new DBHandler(getActivity());

                    playlist_view = getView().findViewById(R.id.playlists_list);
                    playlist_view.setAdapter(null);
                    playlist = dbHandler.getPlaylistNames();

                    PlaylistAdapter customAdapter = new PlaylistAdapter(getActivity().getBaseContext(), playlist);
                    playlist_view.setAdapter(customAdapter);
                }
            }
        });


    public PlaylistsFragment() {}

    public static PlaylistsFragment newInstance(String param1, String param2) {
        PlaylistsFragment fragment = new PlaylistsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * MENU
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.playlists_list) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.playlist_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.d("MENUINFO", info.toString());
        switch (item.getItemId()) {
            case R.id.delete_playlist:
                DBHandler dhHandler = new DBHandler(getActivity());
                dbHandler.deletePlaylist(playlist.get(info.position));

                LibraryFragment fragment = new LibraryFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true)
                        .replace(R.id.navHostFragment, fragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("POSITION");
            allSongs = (ArrayList) getArguments().getSerializable("SONGLIST");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        createPlaylistRow = getView().findViewById(R.id.create_playlist_row);
        dbHandler = new DBHandler(getActivity());

        playlist_view = getView().findViewById(R.id.playlists_list);
        registerForContextMenu(playlist_view);

        playlist = dbHandler.getPlaylistNames();

        PlaylistAdapter customAdapter = new PlaylistAdapter(getActivity().getBaseContext(), playlist);
        playlist_view.setAdapter(customAdapter);
        if(getArguments()!=null){
            createPlaylistRow.setVisibility(View.GONE);
            playlist_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                    // add song to database
                    dbHandler.addNewSong(playlist.get(i), allSongs.get(position).getName());
                    LibraryFragment fragment = new LibraryFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setReorderingAllowed(true)
                            .replace(R.id.navHostFragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
        else{
            playlist_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // show listview of all songs in playlist
                    playlist_songs = dbHandler.getPlaylistSongs(playlist.get(position));
                    Bundle args = new Bundle();
                    args.putStringArrayList("SONGLIST", playlist_songs);
                    args.putString("PLAYLIST_NAME", playlist.get(position));
                    LibraryFragment fragment = new LibraryFragment();
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setReorderingAllowed(true)
                            .replace(R.id.navHostFragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
            createPlaylistRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CreatePlaylistActivity.class);
                    mStartForResult.launch(intent);
                }
            });
        }
    }
}