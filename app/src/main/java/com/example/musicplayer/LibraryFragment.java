package com.example.musicplayer;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    ListView songsList;
    String[] songs;
    ArrayList<File> allSongs;

    File musicFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Music");

    public LibraryFragment() {
    }

    public static LibraryFragment newInstance(String param1, String param2) {
        LibraryFragment fragment = new LibraryFragment();
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
        if(getArguments()==null){
            if (v.getId()==R.id.songsList) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.song_options_menu, menu);
            }
        }
        else{
            if (v.getId()==R.id.songsList) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.playlist_song_menu, menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(getArguments()==null) {
            switch (item.getItemId()) {
                case R.id.add_to_playlist:
                    Bundle args = new Bundle();
                    args.putInt("POSITION", info.position);
                    args.putSerializable("SONGLIST", allSongs);

                    PlaylistsFragment fragment = new PlaylistsFragment();
                    fragment.setArguments(args);
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
        else{
            switch (item.getItemId()) {
                case R.id.delete_from_playlist:
                    DBHandler dbHandler = new DBHandler(getActivity());
                    String playlistName = getArguments().getString("PLAYLIST_NAME");
                    dbHandler.deleteSongFromPlaylist(allSongs.get(info.position).getName(), playlistName);

                    PlaylistsFragment fragment = new PlaylistsFragment();
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allSongs = ((MainActivity)getActivity()).getSong(musicFolder);
        if (getArguments() != null) {
            ArrayList<String> playlist_songs = getArguments().getStringArrayList("SONGLIST");
            ArrayList<File> playlist_files = new ArrayList<>();
            for(int i = 0; i< playlist_songs.size(); i++){
                Log.d("TAG1", String.valueOf(i));
                for(int j = 0; j<allSongs.size(); j++){
                    if(allSongs.get(j).getName().equals(playlist_songs.get(i))){
                        playlist_files.add(allSongs.get(j));
                        break;
                    }
                }
            }
            allSongs = playlist_files;
        }
        Log.d("NUMSONGS", String.valueOf(allSongs.size()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    //checks and requests for permission before accessing music files in the device
    public void runtimePermission(){
        Dexter.withContext(getActivity().getBaseContext()).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                renderSongs();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public void renderSongs(){
        songs = new String[allSongs.size()];

        //add song names to array
        for (int i = 0; i < allSongs.size(); i++) {
            songs[i] = allSongs.get(i).getName()
                    .replace(".mp3", "")
                    .replace(".wav", "");
        }

        SongAdapter customAdapter = new SongAdapter(getActivity().getBaseContext(), songs);
        songsList.setAdapter(customAdapter);
        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                args.putSerializable("SONGLIST", allSongs);
                args.putInt("POSITION", position);

                PlayerFragment fragment = new PlayerFragment();
                fragment.setArguments(args);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true)
                        .replace(R.id.navHostFragment, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        songsList = getView().findViewById(R.id.songsList);
        registerForContextMenu(songsList);
        runtimePermission();
    }
}