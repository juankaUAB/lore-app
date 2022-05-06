package com.example.lore1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Tryit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tryit)

        val boton_tryit = findViewById<Button>(R.id.tryit_texto)
        boton_tryit.setOnClickListener {
            val lanzar = Intent(this, TraduccionTexto::class.java)
            startActivity(lanzar)
        }
    }
}