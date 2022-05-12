package com.example.lore1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Tryit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tryit)

        val boton_tryit_texto = findViewById<Button>(R.id.tryit_texto)
        boton_tryit_texto.setOnClickListener {
            val lanzar = Intent(this, TraduccionTexto::class.java)
            startActivity(lanzar)
        }

        val boton_tryit_audio = findViewById<Button>(R.id.tryit_audio)
        boton_tryit_audio.setOnClickListener {
            val lanzar = Intent(this, TraduccionAudio::class.java)
            startActivity(lanzar)
        }

        val boton_tryit_imagen = findViewById<Button>(R.id.tryit_imagen)
        boton_tryit_imagen.setOnClickListener {
            val lanzar = Intent(this, TraduccionImagen::class.java)
            startActivity(lanzar)
        }
    }
}