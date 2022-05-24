package com.example.lore1

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient
import com.google.cloud.language.v1.LanguageServiceSettings
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import java.io.IOException
import java.io.InputStream


class TraduccionTexto : AppCompatActivity() {
    private var translate: Translate? = null
    private var language: LanguageServiceClient? = null
    private var texto: EditText? = null
    private var boton: Button? = null
    private var traduccion: TextView? = null
    private var select_idioma: Button? = null
    private var boton_sentimientos: Button? = null

    private val options = arrayOf("English","Spanish","Portuguese","Catalan","French","Chinese","German","Russian","Euskera","Japanese","Hindi")
    private var defaultPosition = 0
    private var target_language = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traduccion_texto)

        // Assignamos los componentes del layout de traducción de imagen a las variables correspondientes.
        texto = findViewById(R.id.cajita_texto)
        boton = findViewById(R.id.boton_traducir)
        traduccion = findViewById(R.id.ver_traduccion)
        select_idioma = findViewById(R.id.selec_idioma)
        boton_sentimientos = findViewById(R.id.boton_sentimientos);

        // Definición del boton que al hacer click muestra el menu de selección del idioma
        // al usuario y permite marcar uno para la traducción del texto detectado.
        select_idioma!!.setOnClickListener {
            val builderSingle = AlertDialog.Builder(this)
            builderSingle.setTitle("Select")
            builderSingle.setPositiveButton(getString(android.R.string.ok)) { dialog, _ -> dialog.dismiss() }
            builderSingle.setSingleChoiceItems(options, defaultPosition) { _, which ->
                defaultPosition = which
                when(options[which]) {
                    "English" -> target_language = "en"
                    "Spanish" -> target_language = "es"
                    "Portuguese" -> target_language = "pt"
                    "Catalan" -> target_language = "ca"
                    "French" -> target_language = "fr"
                    "Chinese" -> target_language = "zh-CN"
                    "German" -> target_language = "de"
                    "Russian" -> target_language = "ru"
                    "Euskera" -> target_language = "eu"
                    "Japanese" -> target_language = "ja"
                    "Hindi" -> target_language = "hi"
                }
            }
            builderSingle.show()
        }

        // Definición del boton que al hacer click realiza la traducción del texto
        // segun el idioma de destino seleccionado.
        boton!!.setOnClickListener {
            var resultado = texto!!.text.toString()

            // Comprobamos si hay conexion a internet.
            if (checkInternetConnection()) {

                //If there is internet connection, get translate service and start translation:
                getTranslateService()
                translate()

            } else {

                //If not, display "no connection" warning:
                traduccion!!.text = resources.getString(R.string.no_connection)
            }
        }

        // Definición del boton que al hacer click capta el sentimiento general
        // del texto introducido por el usuario.
        boton_sentimientos!!.setOnClickListener {
            var resultado = texto!!.text.toString()
            if (checkInternetConnection()) {

                //If there is internet connection, get translate service and start translation:
                getSentimentService()
                sentiment_analysis()

            } else {

                //If not, display "no connection" warning:
                traduccion!!.text = resources.getString(R.string.no_connection)
            }
        }
    }

    // Función que llama a la API de Natural Language y asigna sentimiento segun el resultado que devuelva.
    private fun sentiment_analysis() {
        val doc = Document.newBuilder().setContent(texto!!.text.toString()).setType(
            Document.Type.PLAIN_TEXT
        ).build()
        val sentiment = language!!.analyzeSentiment(doc).documentSentiment

        var sentiment_text = "Undeterminated sentiment"
        if (0 < sentiment.score && sentiment.score < 0.25 && sentiment.score > 1) {
            sentiment_text = "Mixed sentiments"
        } else {
            if (0 < sentiment.score && sentiment.score < 0.25 && 0 < sentiment.score && sentiment.score < 1) {
                sentiment_text = "Neutral text without sentiments"
            } else {
                if (sentiment.score > 0.25) {
                    sentiment_text = "So positive text"
                } else {
                    if (sentiment.score < 0) {
                        sentiment_text = "So negative text"
                    }
                }
            }
        }

        val caja_traduccion = findViewById<TextView>(R.id.ver_traduccion)
        caja_traduccion.text = sentiment_text
    }

    // Función que habilita el uso de la API de Natural Language mediante las credenciales del proyecto Cloud.
    private fun getSentimentService() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            var stream : InputStream = resources.openRawResource(R.raw.credentials)
            val credentialsProvider: CredentialsProvider =
                FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(stream))
            var settings = LanguageServiceSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()
            language = LanguageServiceClient.create(settings)

        } catch (ioe: IOException) {
            ioe.printStackTrace()

        }
    }

    // Función que habilita el uso de la API de Text Translation mediante las credenciales del proyecto Cloud.
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

    // Función que llama a la API de Text Translation de Google y devuelve el texto traducido
    // segun el lenguaje de destino escogido por el usuario.
    private fun translate() {

        //Get input text to be translated:
        val originalText: String = texto!!.text.toString()
        val translation = translate!!.translate(originalText, Translate.TranslateOption.targetLanguage(target_language), Translate.TranslateOption.model("base"))

        //Translated text and original text are set to TextViews:
        traduccion!!.text = translation.translatedText

    }

    // Función que comprueba si el dispositivo dispone o no de conexion a internet.
    private fun checkInternetConnection(): Boolean {

        //Check internet connection:
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

        //Means that we are connected to a network (mobile or wi-fi)
        return activeNetwork?.isConnected == true

    }
}