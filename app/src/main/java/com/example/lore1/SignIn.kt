package com.example.lore1

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.gax.rpc.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.io.IOException

@Suppress("DEPRECATION")

class SignIn : AppCompatActivity() {
    private val GOOGLE_SIGNIN = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                Settings.Global.getString(
                    contentResolver,
                    R.string.app_name.toString()
                )
            ).requestEmail().build();

        val googleClient = GoogleSignIn.getClient(this, googleConf);
        googleClient.signOut()
        startActivityForResult(googleClient.signInIntent, GOOGLE_SIGNIN);

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGNIN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try{
                val account = task.getResult(ApiException::class.java)

                if(account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful){
                            val lanzar = Intent(this, Tryit_log::class.java)
                            startActivity(lanzar)
                        } else{
                            print("Error logging with Google");

                        }
                    }

                }
            }catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
    }
}