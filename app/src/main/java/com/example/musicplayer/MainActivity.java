package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    boolean shuffle, repeat;
    int currentPlayerBg = R.drawable.music_player_bg1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Bottom Navigation View.
        BottomNavigationView navView = findViewById(R.id.bottomNav_view);

        //Pass the ID's of Different destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_library, R.id.navigation_player, R.id.navigation_playlists)
                .build();

        //Initialize NavController.
        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }

    public boolean getShuffleState(){
        return shuffle;
    }
    public void setShuffleState(boolean state){
        shuffle = state;
    }
    public boolean getRepeatState(){
        return repeat;
    }
    public void setRepeatState(boolean state){
        repeat = state;
    }

    public void setPlayerBg(int playerBg){
        currentPlayerBg = playerBg;
    }

    public int getCurrPlayerBg(){
        return currentPlayerBg;
    }


    public ArrayList<File> getSong(File songsFolder){
        ArrayList<File> songsList = new ArrayList<>();
        File[] songs = songsFolder.listFiles();

        // recursively get each song file from a nested directory
        for(File songFile:songs){
            if(songFile.isDirectory() && !songFile.isHidden()){
                getSong(songFile);
            }
            else{
                // get all the .mp3 and .wav files
                if(songFile.getName().endsWith(".mp3") ||
                        songFile.getName().endsWith(".wav")){
                    songsList.add(songFile);
                }
            }
        }
        return songsList;
    }
}