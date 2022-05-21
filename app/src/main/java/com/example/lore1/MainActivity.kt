package com.example.lore1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boton_tryit = findViewById<Button>(R.id.try_it)
        boton_tryit.setOnClickListener {
            val lanzar = Intent(this, Tryit::class.java)
            startActivity(lanzar)
        }

        val boton_tryit_log = findViewById<Button>(R.id.login)
        boton_tryit_log.setOnClickListener {
            val lanzar = Intent(this, Tryit_log::class.java)
            startActivity(lanzar)
        }

        val boton_signin = findViewById<Button>(R.id.signin)
        boton_signin.setOnClickListener {
            val lanzar = Intent(this, SignIn::class.java)
            startActivity(lanzar)
        }
    }
}