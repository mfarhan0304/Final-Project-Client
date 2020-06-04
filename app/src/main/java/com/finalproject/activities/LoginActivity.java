package com.finalproject.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finalproject.APIService;
import com.finalproject.AuthResponse;
import com.finalproject.PermissionUtil;
import com.finalproject.R;
import com.github.squti.androidwaverecorder.WaveRecorder;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.finalproject.PermissionUtil.isPermissionGranted;

public class LoginActivity extends AppCompatActivity {

    private EditText username_input;
    private Button login_button;
    private ImageButton record_button, play_button, delete_button;
    private SeekBar voice_progress;
    private LinearLayout audio_player_box, record_button_box;
    private TextView no_account_text;

    private File voiceFile;

    private WaveRecorder waveRecorder = null;
    private boolean isRecording = false;
    private boolean isRecorded = false;
    private String fileName = "test.wav";
    private String filePath = "";
    private MediaPlayer mediaPlayer = null;
    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        bindView();
        bindData();
        bindListener();
    }

    private void bindView() {
        username_input = findViewById(R.id.inputUsername);
        audio_player_box = findViewById(R.id.audioPlayerBox);
        play_button = findViewById(R.id.playButton);
        delete_button = findViewById(R.id.deleteButton);
        voice_progress = findViewById(R.id.loginSeekBar);
        record_button_box = findViewById(R.id.recordButtonBox);
        record_button = findViewById(R.id.recordButton);
        no_account_text = findViewById(R.id.noAccountButton);
        login_button = findViewById(R.id.loginButton);
    }

    private void bindData() {
        if (!isPermissionGranted(this, this,
                PermissionUtil.allPermissions)) {
            if (getIntent() == null) finish();
        }

        filePath = getFilesDir() + "/" + fileName;
        handler = new Handler();
    }

    private void bindListener() {
        record_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                    isRecording = false;
                    isRecorded = true;
                    viewChanges(View.GONE, View.VISIBLE);
                    record_button.setColorFilter(Color.argb(0,0,0,0));
                } else {
                    startRecording();
                    isRecording = true;
                    record_button.setColorFilter(Color.argb(255,0,170,77));
                }
            }
        });

        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                voice_progress.setMax(mediaPlayer.getDuration());
                handler.postDelayed(updateVoiceTime, 100);
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecorded = false;

                mediaPlayer.release();
                record_button.setColorFilter(Color.argb(255,0,0,0));
                viewChanges(View.VISIBLE, View.GONE);
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                String username = username_input.getText().toString().trim();
                if (!username.isEmpty() && isRecorded) {
                    login(username);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Kindly fill username and record your voice first",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        no_account_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private Runnable updateVoiceTime = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                voice_progress.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 100);
            }
        }
    };

    private void viewChanges(int rec, int others) {
        record_button_box.setVisibility(rec);
        audio_player_box.setVisibility(others);
    }

    private void startRecording() {
        waveRecorder = new WaveRecorder(filePath);
        waveRecorder.startRecording();
    }

    private void stopRecording() {
        waveRecorder.stopRecording();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String usernameInput) {
        RequestBody username = RequestBody.create(
                MediaType.parse("text/plain; charset=utf-8"),
                usernameInput
        );
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("audio/wav"),
                voiceFile
        );
        MultipartBody.Part voice = MultipartBody.Part.createFormData(
                "voice", fileName, requestFile
        );
        APIService.getAPIService().login(username, voice).enqueue(new Callback<AuthResponse>() {
            Toast toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_LONG);
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    toast.setText("Welcome " + response.body().getUsername());
                } else {
                    toast.setText(response.body().getStatusMessage());
                }
                toast.show();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                toast.setText("There's a trouble");
                toast.show();
            }
        });
    }
}
