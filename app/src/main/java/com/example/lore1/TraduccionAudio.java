package com.example.lore1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.ByteString;

public class TraduccionAudio extends AppCompatActivity {
    MediaRecorder recorder;
    File audiofile = null;
    Uri audiofile_uri = null;
    static final String TAG = "MediaRecording";
    FloatingActionButton startButton,stopButton;
    TextView texto_escuchado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traduccion_audio);
        startButton = (FloatingActionButton) findViewById(R.id.original_audio_button);
        stopButton = (FloatingActionButton) findViewById(R.id.original_pause_button);
        texto_escuchado = findViewById(R.id.audio_escuchado);
    }


    public void startRecording(View view) throws IOException {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        //Creating file
        File dir = Environment.getDataDirectory();
        try {
            audiofile = File.createTempFile("sound", ".3gp", null);
        } catch (IOException e) {
            Log.e(TAG, "external storage access error");
            return;
        }
        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.prepare();
        recorder.start();
    }

    public void stopRecording(View view) throws IOException {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        //stopping recorder
        recorder.stop();
        recorder.reset();
        recorder.release();
        //after stopping the recorder, create the sound file and add it to media library.
        addRecordingToMediaLibrary();
        speechtotext();

    }

    protected void addRecordingToMediaLibrary() {
        //creating content values of size 4
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());

        //creating content resolver and storing it in the external content uri
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);
        audiofile_uri = newUri;

        //sending broadcast message to scan the media file so that it can be available
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void speechtotext() throws IOException {
        InputStream stream = getResources().openRawResource(R.raw.credentials);
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(stream));
        SpeechSettings setting = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        SpeechClient client = SpeechClient.create(setting);

        Path path = Paths.get(audiofile.getAbsolutePath());
        byte[] content = Files.readAllBytes(path);

        RecognitionConfig recConfig =
                RecognitionConfig.newBuilder()
                        // encoding may either be omitted or must match the value in the file header
                        .setEncoding(RecognitionConfig.AudioEncoding.AMR_WB)
                        .setLanguageCode("en-US")
                        // sample rate hertz may be either be omitted or must match the value in the file
                        // header
                        .setSampleRateHertz(16000)
                        .setModel("video")
                        .build();

        RecognitionAudio recognitionAudio =
                RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();
        // Performs speech recognition on the audio file
        RecognizeResponse recognizeResponse = client.recognize(recConfig, recognitionAudio);
        // Just print the first result here.
        SpeechRecognitionResult result = recognizeResponse.getResultsList().get(0);
        // There can be several alternative transcripts for a given chunk of speech. Just use the
        // first (most likely) one here.
        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        texto_escuchado.setText(alternative.getTranscript());
    }
}