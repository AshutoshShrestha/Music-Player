package com.example.musicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreatePlaylistActivity extends AppCompatActivity {

    Button createBtn, cancelBtn;
    TextView playlistName;

    private DBHandler dbHandler;

    @Override
    protected void onDestroy() {
        dbHandler.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        playlistName = findViewById(R.id.new_playlist_name);
        cancelBtn = findViewById(R.id.cancel_playlist_btn);
        createBtn = findViewById(R.id.create_playlist_btn);

        // creating a new dbhandler class
        // and passing our context to it.
        dbHandler = new DBHandler(CreatePlaylistActivity.this);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_playlist = playlistName.getText().toString();

                if (name_playlist.isEmpty()) {
                    Toast.makeText(CreatePlaylistActivity.this, "Please enter playlist name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // on below line we are calling a method to add new
                // course to sqlite data and pass all our values to it.
                dbHandler.addNewPlaylist(name_playlist);

                Toast.makeText(CreatePlaylistActivity.this, "Playlist created", Toast.LENGTH_SHORT).show();
                playlistName.setText("");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }, 2000);
            }
        });
    }

}
