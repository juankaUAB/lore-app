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
import com.google.protobuf.ByteString;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

public class TraduccionImagen extends AppCompatActivity {

    ImageButton Button_camara_image;
    ImageView slot_camara;
    String ruta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traduccion_imagen);
        Button_camara_image = findViewById(R.id.Button_camara_image);
        slot_camara = findViewById(R.id.slot_camara);

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

    //Este método captura la imagen y la envia al ImageView.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        //Validamos el resultado de la actividad
        if(requestCode == 1 && resultCode == RESULT_OK){
            //Obtenemos resultado
            //Bundle extras = data.getExtras();
            //Bitmap imgBitmap = (Bitmap) extras.get("data");
            Bitmap imgBitmap = BitmapFactory.decodeFile(ruta);

            //Mostramos imagen en el imageView
            slot_camara.setImageBitmap(imgBitmap);
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