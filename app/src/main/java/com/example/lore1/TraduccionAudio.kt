package com.example.lore1

import android.media.MediaPlayer
import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.auto.value.extension.AutoValueExtension
import java.io.File
import java.io.IOException
import android.media.MediaRecorder as MediaRecorder

class TraduccionAudio : AppCompatActivity(), MediaPlayer.OnCompletionListener {

    lateinit var recorder: MediaRecorder
    lateinit var player: MediaPlayer
    lateinit var archivo: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traduccion_audio)

        var boton_iniciar_audio: FloatingActionButton = findViewById(R.id.original_audio_button)
        var boton_pausar_audio: FloatingActionButton = findViewById(R.id.original_pause_button)

        boton_iniciar_audio.setOnClickListener {
            recorder = MediaRecorder()
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            val path = File(Environment.getExternalStorageDirectory()
                .getPath())
            try {
                archivo = File.createTempFile("temporal", ".wav", path)
            } catch (e: IOException) {
            }

            recorder.setOutputFile(archivo.absolutePath)
            try {
                recorder.prepare()
            } catch (e: IOException) {
            }

            recorder.start()
            boton_iniciar_audio.setEnabled(false)
            boton_pausar_audio.setEnabled(true)
        }

        boton_pausar_audio.setOnClickListener {
            recorder.stop()
            recorder.release()
            player = MediaPlayer()
            player.setOnCompletionListener(this)
            try {
                player.setDataSource(archivo.absolutePath)
            } catch (e: IOException) {
            }

            try {
                player.prepare()
            } catch (e: IOException) {
            }

            boton_iniciar_audio.setEnabled(true)
            boton_pausar_audio.setEnabled(false)
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        var boton_iniciar_audio: FloatingActionButton = findViewById(R.id.original_audio_button)
        boton_iniciar_audio.setEnabled(true)
        var boton_pausar_audio: FloatingActionButton = findViewById(R.id.original_pause_button)
        boton_pausar_audio.setEnabled(true)
    }
}