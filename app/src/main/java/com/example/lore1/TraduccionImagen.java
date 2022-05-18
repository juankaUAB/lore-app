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
    Button boton_galeria_imagen, traduccir;
    ImageView slot_camara;
    TextView ver_traduccion_imagen;
    String ruta;
    Bitmap imgBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traduccion_imagen);
        Button_camara_image = findViewById(R.id.Button_camara_image);
        boton_galeria_imagen = findViewById(R.id.boton_galeria_imagen);
        traduccir = findViewById(R.id.boton_traducir_imagen);
        slot_camara = findViewById(R.id.slot_camara);
        ver_traduccion_imagen = findViewById(R.id.ver_traduccion_imagen);

        //Solicitar permisos de usuario para camara y acceso a galeria.
        if(ContextCompat.checkSelfPermission(TraduccionImagen.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(TraduccionImagen.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(TraduccionImagen.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, 1000);
        }

        Button_camara_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camara();
            }
        });

        boton_galeria_imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarImagen();
            }
        });

        traduccir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    detectText();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void detectText() throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String filePath = ruta;
        detectText(filePath);
    }

    // Detects text in the specified image.
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
                    ver_traduccion_imagen.setText("Error: %s%n", TextView.BufferType.valueOf(res.getError().getMessage()));
                    return;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    ver_traduccion_imagen.setText("Text: %s%n", TextView.BufferType.valueOf(annotation.getDescription()));
                }
            }
        }
    }

    //Metodo para abrir o activar la camara.
    private void camara(){
        //La activamos con un intent que permetira capturar la imagen.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Validamos si el recurso esta disponible.
        if(intent.resolveActivity(getPackageManager())!= null){
            File fotoArchivo = null;
            try{
                fotoArchivo = GuardarImagen();
            }catch(IOException ex){
                Log.e("error", ex.toString());
            }
            //Validamos si no hay otra imagen tomada ya
            if(fotoArchivo != null){
                Uri uri = FileProvider.getUriForFile(this, "com.lore1.mycamera.fileprovider", fotoArchivo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, 1);
            }
        }
    }

    private void cargarImagen(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent, "Seleccione la Aplicación"), 10);
    }

    //Este método captura la imagen y la envia al ImageView.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        //Validamos el resultado de la actividad
        if(requestCode == 1 && resultCode == RESULT_OK){
            //Obtenemos resultado
            //Bundle extras = data.getExtras();
            //Bitmap imgBitmap = (Bitmap) extras.get("data");
            imgBitmap = BitmapFactory.decodeFile(ruta);

            //Mostramos imagen en el imageView
            slot_camara.setImageBitmap(imgBitmap);
        }

        if(requestCode == 10 && resultCode == RESULT_OK){
            Uri path = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(path, projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            ruta = cursor.getString(columnIndex);
            cursor.close();

            //File imFile = new File(path.getPath());//create path from uri
            //ruta = imFile.getAbsolutePath();
            slot_camara.setImageURI(path);
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

}