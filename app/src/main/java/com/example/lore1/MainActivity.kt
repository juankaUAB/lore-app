package com.example.lore1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boton_tryit = findViewById<Button>(R.id.try_it)
        boton_tryit.setOnClickListener {
            val lanzar = Intent(this, Tryit::class.java)
            startActivity(lanzar)
        }
    }
}