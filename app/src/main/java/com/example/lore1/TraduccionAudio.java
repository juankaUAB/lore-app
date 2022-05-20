package com.example.lore1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.translation.TranslationRequest;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.speech.v1.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3beta1.TranslationServiceSettings;
import com.google.protobuf.ByteString;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

public class TraduccionAudio extends AppCompatActivity {
    MediaRecorder recorder;
    File audiofile = null;
    Uri audiofile_uri = null;
    static final String TAG = "MediaRecording";
    FloatingActionButton startButton,stopButton;
    TextView texto_escuchado;
    CharSequence[] options = new CharSequence[] {"English","Spanish","Portuguese","Catalan","French","Chinese","German","Russian","Euskera","Japanese","Hindi"};
    CharSequence[] options_origin = new CharSequence[] {"English","Spanish","Portuguese","Catalan","French","Chinese","German","Russian","Euskera","Japanese","Hindi"};
    int defaultOption = 0;
    String target_language, input_language;

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
                        .setLanguageCode(input_language) //input_language ".
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

    public void setOriginLanguage(View view) {
        AlertDialog.Builder builderSingle_origin = new AlertDialog.Builder(this);
        builderSingle_origin.setTitle("Select recorder language");
        builderSingle_origin.setPositiveButton(android.R.string.ok, (dialog2, which) -> { dialog2.dismiss(); });
        builderSingle_origin.setSingleChoiceItems(options_origin, defaultOption, (dialog3, item_origin) -> {
            defaultOption = item_origin;
            if ("English".equals(options_origin[item_origin])) {
                input_language = "en-US";
            } else if ("Spanish".equals(options_origin[item_origin])) {
                input_language = "es-ES";
            } else if ("Portuguese".equals(options_origin[item_origin])) {
                input_language = "pt-PT";
            } else if ("Catalan".equals(options_origin[item_origin])) {
                input_language = "ca-ES";
            } else if ("French".equals(options_origin[item_origin])) {
                input_language = "fr-FR";
            } else if ("Chinese".equals(options_origin[item_origin])) {
                input_language = "zh-CN";
            } else if ("German".equals(options_origin[item_origin])) {
                input_language = "de-DE";
            } else if ("Russian".equals(options_origin[item_origin])) {
                input_language = "ru-RU";
            } else if ("Euskera".equals(options_origin[item_origin])) {
                input_language = "eu-ES";
            } else if ("Japanese".equals(options_origin[item_origin])) {
                input_language = "ja-JP";
            } else if ("Hindi".equals(options_origin[item_origin])) {
                input_language = "hi-IN";
            }
        });
        builderSingle_origin.show();
    }

    public void setLanguage(View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select translation language");
        builderSingle.setPositiveButton(android.R.string.ok, (dialog, which) -> { dialog.dismiss(); });
        builderSingle.setSingleChoiceItems(options, defaultOption, (dialog1, item) -> {
            defaultOption = item;
            if ("English".equals(options[item])) {
                target_language = "en";
            } else if ("Spanish".equals(options[item])) {
                target_language = "es";
            } else if ("Portuguese".equals(options[item])) {
                target_language = "pt";
            } else if ("Catalan".equals(options[item])) {
                target_language = "ca";
            } else if ("French".equals(options[item])) {
                target_language = "fr";
            } else if ("Chinese".equals(options[item])) {
                target_language = "zh-CN";
            } else if ("German".equals(options[item])) {
                target_language = "de";
            } else if ("Russian".equals(options[item])) {
                target_language = "ru";
            } else if ("Euskera".equals(options[item])) {
                target_language = "eu";
            } else if ("Japanese".equals(options[item])) {
                target_language = "ja";
            } else if ("Hindi".equals(options[item])) {
                target_language = "hi";
            }
        });
        builderSingle.show();
    }

    public void translate(View view) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        InputStream stream = getResources().openRawResource(R.raw.credentials);
        TranslateOptions translate_options = TranslateOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(stream)).build();
        Translate translate = translate_options.getService();

        Translation translation = translate.translate(
                String.valueOf(texto_escuchado.getText()),
                Translate.TranslateOption.targetLanguage(target_language),
                Translate.TranslateOption.model("base"));
        TextView caja_traduccion = findViewById(R.id.audio_traducido);
        caja_traduccion.setText(translation.getTranslatedText());
    }

    public void sentiment_analysis(View view) throws IOException {
        InputStream stream = getResources().openRawResource(R.raw.credentials);
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(stream));
        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        LanguageServiceClient language = LanguageServiceClient.create(settings);

        Document doc = Document.newBuilder().setContent((String) texto_escuchado.getText()).setType(Type.PLAIN_TEXT).build();
        Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();

        String sentiment_text = "Undeterminated sentiment";
        if (0 < sentiment.getScore() &&  sentiment.getScore() < 0.25 && sentiment.getScore() > 1)
        {
            sentiment_text = "Mixed sentiments";
        }
        else {
            if (0 < sentiment.getScore() && sentiment.getScore() < 0.25 && 0 < sentiment.getScore() && sentiment.getScore() < 1) {
                sentiment_text = "Neutral text without sentiments";
            }
            else {
                if (sentiment.getScore() > 0.25) {
                    sentiment_text = "So positive text";
                } else {
                    if (sentiment.getScore() < 0) {
                        sentiment_text = "So negative text";
                    }
                }
            }
        }

        TextView caja_traduccion = findViewById(R.id.audio_traducido);
        caja_traduccion.setText(sentiment_text);
    }
}