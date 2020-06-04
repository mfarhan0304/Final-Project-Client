package com.finalproject.activities;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finalproject.APIService;
import com.finalproject.AuthResponse;
import com.finalproject.R;
import com.finalproject.Voice;
import com.finalproject.VoiceAdapter;
import com.github.squti.androidwaverecorder.WaveRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput;
    private RadioGroup genderRadioGroup;
    private ListView voiceListView;
    private LinearLayout recordButtonBox;
    private ImageButton recordButton;
    private Button registerButton;
    private TextView haveAccountText;

    private ArrayList<Voice> voiceArrayList;
    private VoiceAdapter voiceAdapter;

    private boolean isRecording;
    private WaveRecorder waveRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindView();
        bindData();
        bindListener();
    }

    private void bindView() {
        setContentView(R.layout.activity_register);
        usernameInput = findViewById(R.id.inputUsernameRegister);
        genderRadioGroup = findViewById(R.id.radioGender);
        voiceListView = findViewById(R.id.voiceListRegister);
        recordButtonBox = findViewById(R.id.recordButtonRegisterBox);
        recordButton = findViewById(R.id.recordButtonRegister);
        registerButton = findViewById(R.id.registerButton);
        haveAccountText = findViewById(R.id.haveAccountText);
    }

    private void bindData() {
        voiceArrayList = new ArrayList<>();
        voiceAdapter = new VoiceAdapter(this, voiceArrayList);
        voiceListView.setAdapter(voiceAdapter);
    }

    private void bindListener() {
        voiceAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recordButtonBox.setVisibility(voiceArrayList.size() == 5 ? View.GONE : View.VISIBLE);

                int totalHeight = 0;
                for (int i = 0; i < voiceAdapter.getCount(); i++) {
                    View listItem = voiceAdapter.getView(i, null, voiceListView);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                ViewGroup.LayoutParams params = voiceListView.getLayoutParams();
                params.height = totalHeight + (voiceListView.getDividerHeight() * (voiceAdapter.getCount()));
                voiceListView.setLayoutParams(params);
                voiceListView.requestLayout();
            }
        });
        recordButtonBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                    recordButton.setColorFilter(Color.argb(0,0,0,0));
                    recordButtonBox.setBackgroundResource(R.drawable.border);
                } else {
                    startRecording();
                    recordButton.setColorFilter(Color.argb(255,0,170,77));
                    recordButtonBox.setBackgroundResource(R.drawable.border_active);
                }
                isRecording = !isRecording;
            }
        });
        haveAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                String username = usernameInput.getText().toString().trim();

                if (!username.isEmpty() && selectedId != -1 && voiceArrayList.size() == 5) {
                    RadioButton selectedButton = findViewById(selectedId);
                    String gender = selectedButton.getText().toString();
                    register(username, gender);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Kindly fill username, gender, and record 5 times of your voice first",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startRecording() {
        String filename = "enroll" + voiceArrayList.size() + ".wav";
        String filePath = getFilesDir() + "/" + filename;

        waveRecorder = new WaveRecorder(filePath);
        waveRecorder.startRecording();
    }

    private void stopRecording() {
        String filename = "enroll" + voiceArrayList.size() + ".wav";
        String filePath = getFilesDir() + "/" + filename;

        waveRecorder.stopRecording();
        voiceArrayList.add(new Voice(filePath, filename));
        voiceAdapter.notifyDataSetChanged();
    }

    private void register(String username, String gender) {
        RequestBody requestUsername = RequestBody.create(
                MediaType.parse("text/plain; charset=utf-8"),
                username
        );
        RequestBody requestGender = RequestBody.create(
                MediaType.parse("text/plain; charset=utf-8"),
                gender
        );
        List<MultipartBody.Part> requestVoices = new ArrayList<>();
        for (Voice voice: voiceArrayList) {
            File voiceFile = new File(voice.getFilePath());
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("audio/wav"),
                    voiceFile
            );
            MultipartBody.Part requestVoice = MultipartBody.Part.createFormData(
                    "voice", voice.getName(), requestFile
            );
            requestVoices.add(requestVoice);
        }

        APIService.getAPIService().register(requestUsername, requestGender, requestVoices).enqueue(new Callback<AuthResponse>() {
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
