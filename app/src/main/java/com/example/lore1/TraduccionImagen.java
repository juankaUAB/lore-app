package com.example.lore1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import java.util.ArrayList;

public class TraduccionImagen extends AppCompatActivity {

    ImageButton Button_camara_image;
    Button boton_galeria_imagen, traduccir, sentimiento;
    ImageView slot_camara;
    TextView ver_traduccion_imagen;
    String ruta;
    Bitmap imgBitmap;
    CharSequence[] options = new CharSequence[] {"English","Spanish","Portuguese","Catalan","French","Chinese","German","Russian","Euskera","Japanese","Hindi"};
    int defaultOption = 0;
    String target_language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traduccion_imagen);

        // Assignamos los componentes del layout de traducción de imagen a las variables correspondientes.
        Button_camara_image = findViewById(R.id.Button_camara_image);
        boton_galeria_imagen = findViewById(R.id.boton_galeria_imagen);
        traduccir = findViewById(R.id.boton_traducir_imagen);
        sentimiento = findViewById(R.id.boton_sentimientos_imagen);
        slot_camara = findViewById(R.id.slot_camara);
        ver_traduccion_imagen = findViewById(R.id.ver_traduccion_imagen);

        // Definición del boton que al hacer click utiliza la camara del dispositivo para tomar imagenes.
        Button_camara_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camara();
            }
        });

        // Definición del boton que al hacer click accede a la galeria para seleccionar una imagen.
        boton_galeria_imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarImagen();
            }
        });

        // Definición del boton que al hacer click capta y traduce el texto de la imagen seleccionada
        // segun el idioma escogido.
        traduccir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    detectText();
                    translate();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Definición del boton que al hacer click capta el texto de la imagen seleccionada
        // y extrae el sentimiento generalizado de ese texto.
        sentimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    detectText();
                    sentiment_analysis();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Metodo que muestra el menu de selección del idioma al usuario
    // y permite marcar uno para la traducción del texto detectado.
    public void setLanguage(View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select");
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

    // Metodo que hace de constructor para detectText.
    public void detectText() throws IOException {
        String filePath = ruta;
        detectText(filePath);
    }

    // Metodo que habilita el uso de la API Vision de Google mediante las credenciales del proyecto Cloud
    // y devuelve el texto detectado de la imagen seleccionada.
    public void detectText(String filePath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.

        InputStream stream = getResources().openRawResource(R.raw.credentials);
        Credentials myCredentials = ServiceAccountCredentials.fromStream(stream);

        ImageAnnotatorSettings imageAnnotatorSettings =
                ImageAnnotatorSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials))
                        .build();


        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(imageAnnotatorSettings)) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    String error_msg = "Error: %s%n" + res.getError().getMessage();
                    ver_traduccion_imagen.setText(error_msg);
                    return;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                String text_detected = res.getTextAnnotations(0).getDescription();
                ver_traduccion_imagen.setText(text_detected);
            }
        }
    }

    // Metodo que habilita el uso de la API de Text Translation mediante las credenciales del proyecto Cloud
    // y devuelve el texto detectado traducido segun el lenguaje de destino escogido por el usuario.
    public void translate() throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        InputStream stream = getResources().openRawResource(R.raw.credentials);
        TranslateOptions translate_options = TranslateOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(stream)).build();
        Translate translate = translate_options.getService();

        Translation translation = translate.translate(
                String.valueOf(ver_traduccion_imagen.getText()),
                Translate.TranslateOption.targetLanguage(target_language),
                Translate.TranslateOption.model("base"));
        ver_traduccion_imagen.setText(translation.getTranslatedText());
    }

    // Metodo que habilita el uso de la API de Natural Language mediante las credenciales del proyecto Cloud
    // y asigna sentimiento general al texto detectado segun el resultado que devuelva.
    public void sentiment_analysis() throws IOException {
        InputStream stream = getResources().openRawResource(R.raw.credentials);
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(stream));
        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        LanguageServiceClient language = LanguageServiceClient.create(settings);

        Document doc = Document.newBuilder().setContent((String) ver_traduccion_imagen.getText()).setType(Type.PLAIN_TEXT).build();
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
        ver_traduccion_imagen.setText(sentiment_text);
    }


    // Metodo para abrir o activar la camara.
    private void camara(){
        //Activamos la camara con un intent que permetira capturar la imagen.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Validamos si el recurso esta disponible.
        if(intent.resolveActivity(getPackageManager())!= null){
            File fotoArchivo = null;

            // Una vez tomada la fotografia intentamos guardarla en la galeria.
            try{
                fotoArchivo = GuardarImagen();
            }catch(IOException ex){
                Log.e("error", ex.toString());
            }

            //Validamos si la imagen ha podido guardarse y en caso de ser asi generamos una actividad de camara.
            if(fotoArchivo != null){
                Uri uri = FileProvider.getUriForFile(this, "com.lore1.mycamera.fileprovider", fotoArchivo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, 1);
            }
        }
    }

    //Este método guarda la imagen en la galeria.
    private File GuardarImagen() throws IOException{
        String NombreFoto = "foto_";
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File foto = File.createTempFile(NombreFoto, ".jpg", directorio);
        ruta = foto.getAbsolutePath();
        return foto;
    }

    //Este método carga una imagen de la galeria y genera una actividad de imagen cargada.
    private void cargarImagen(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent, "Seleccione la Aplicación"), 10);
    }

    //Este método captura el resultado de la actividad de la imagen y la envia al ImageView.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // Si la actividad proviene de hacer una fotografia con la camara la tratamos aqui.
        if(requestCode == 1 && resultCode == RESULT_OK){
            //Obtenemos resultado
            imgBitmap = BitmapFactory.decodeFile(ruta);

            //Mostramos imagen en el imageView
            slot_camara.setImageBitmap(imgBitmap);
        }

        // Si la actividad proviene de cargar una imagen de la galeria la tratamos aqui.
        if(requestCode == 10 && resultCode == RESULT_OK){
            //Obtenemos resultado
            Uri path = data.getData();

            //Obtenemos y tratamos el path absoluto de la imagen del dispositivo.
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(path, projection, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            ruta = cursor.getString(columnIndex);
            cursor.close();

            //Mostramos imagen en el imageView
            slot_camara.setImageURI(path);
        }
    }
}