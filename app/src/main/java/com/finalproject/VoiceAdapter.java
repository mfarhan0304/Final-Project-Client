package com.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.ArrayList;

public class VoiceAdapter extends BaseAdapter {

    private ArrayList<Voice> voices;
    private LayoutInflater layoutInflater;
    private MediaPlayer mediaPlayer;
    private Handler handler;

    public VoiceAdapter(Context context, ArrayList<Voice> voices) {
        this.voices = voices;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return voices.size();
    }

    @Override
    public Object getItem(int i) {
        return voices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder {
        ImageButton playButton, deleteButton;
        SeekBar seekBar;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;

        viewHolder = new ViewHolder();
        view = layoutInflater.inflate(R.layout.row_voice, null);
        viewHolder.playButton = view.findViewById(R.id.playButton);
        viewHolder.seekBar = view.findViewById(R.id.voiceSeekBar);
        viewHolder.deleteButton = view.findViewById(R.id.deleteButton);
        view.setTag(position);

        Voice voice = voices.get(position);

        mediaPlayer = new MediaPlayer();
        handler = new Handler();
        try {
            mediaPlayer.setDataSource(voice.getFilePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                viewHolder.seekBar.setMax(mediaPlayer.getDuration());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer.isPlaying()) {
                            viewHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            handler.postDelayed(this, 100);
                        }
                    }
                }, 100);
            }
        });

        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                voices.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
