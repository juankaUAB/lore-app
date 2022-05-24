package com.example.lore1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.lore1.MainActivity.Companion.EXTRA_NAME
import com.example.lore1.databinding.ActivityTryitLogBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Tryit_log : AppCompatActivity() {

    private lateinit var binding: ActivityTryitLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTryitLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtenemos el nombre de usuario y lo mostramos en el textview de username.
        var nombreUser = intent.getStringExtra(EXTRA_NAME)
        binding.username.setText(nombreUser)

        // Definición del boton que al hacer click te lleva al layout de la traduccion de texto.
        val boton_tryit_texto = findViewById<Button>(R.id.tryit_log_texto)
        boton_tryit_texto.setOnClickListener {
            val lanzar = Intent(this, TraduccionTexto::class.java)
            startActivity(lanzar)
        }

        // Definición del boton que al hacer click te lleva al layout de la traduccion de audio.
        val boton_tryit_audio = findViewById<Button>(R.id.tryit_log_audio)
        boton_tryit_audio.setOnClickListener {
            val lanzar = Intent(this, TraduccionAudio::class.java)
            startActivity(lanzar)
        }

        // Definición del boton que al hacer click te lleva al layout de la traduccion de imagen.
        val boton_tryit_imagen = findViewById<Button>(R.id.tryit_log_imagen)
        boton_tryit_imagen.setOnClickListener {
            val lanzar = Intent(this, TraduccionImagen::class.java)
            startActivity(lanzar)
        }

        // Definición del boton que al hacer click te lleva al layout del historial del usuario.
        val boton_historial = findViewById<Button>(R.id.tryit_log_historial)
        boton_historial.setOnClickListener {
            val lanzar = Intent(this, Historial::class.java)
            startActivity(lanzar)
        }

        // Definición del boton que al hacer click realiza el sign out y te lleva al layout del mainActivity.
        binding.tryitLogSignout.setOnClickListener {

            Firebase.auth.signOut()

            val lanzar = Intent(this, MainActivity::class.java)
            startActivity(lanzar)
        }

    }
}