package com.example.lore1

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import java.io.IOException
import java.io.InputStream

class TraduccionTexto : AppCompatActivity() {
    private var translate: Translate? = null
    private var texto: EditText? = null
    private var boton: Button? = null
    private var traduccion: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traduccion_texto)

        texto = findViewById(R.id.cajita_texto)
        boton = findViewById(R.id.boton_traducir)
        traduccion = findViewById(R.id.ver_traduccion)

        boton!!.setOnClickListener {
            var resultado = texto!!.text.toString()
            if (checkInternetConnection()) {

                //If there is internet connection, get translate service and start translation:
                getTranslateService()
                translate()

            } else {

                //If not, display "no connection" warning:
                traduccion!!.text = resources.getString(R.string.no_connection)
            }
        }
    }

    private fun getTranslateService() {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            var stream : InputStream = resources.openRawResource(R.raw.credentials)
            var myCredentials: GoogleCredentials = GoogleCredentials.fromStream(stream)
            var translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build()
            translate = translateOptions.service

        } catch (ioe: IOException) {
            ioe.printStackTrace()

        }

    }

    private fun translate() {

        //Get input text to be translated:
        val originalText: String = texto!!.text.toString()
        val translation = translate!!.translate(originalText, Translate.TranslateOption.targetLanguage("en"), Translate.TranslateOption.model("base"))

        //Translated text and original text are set to TextViews:
        traduccion!!.text = translation.translatedText

    }

    private fun checkInternetConnection(): Boolean {

        //Check internet connection:
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

        //Means that we are connected to a network (mobile or wi-fi)
        return activeNetwork?.isConnected == true

    }
}