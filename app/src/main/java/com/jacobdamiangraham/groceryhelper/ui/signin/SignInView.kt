package com.jacobdamiangraham.groceryhelper.ui.signin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jacobdamiangraham.groceryhelper.MainActivity
import com.jacobdamiangraham.groceryhelper.databinding.ActivitySigninBinding

class SignInView : AppCompatActivity() {

    private lateinit var activitySigninBinding: ActivitySigninBinding
    private lateinit var firebaseAuthentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySigninBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(activitySigninBinding.root)

        firebaseAuthentication = FirebaseAuth.getInstance()

        activitySigninBinding.signInButton.setOnClickListener {
            attemptLogin()
        }

        activitySigninBinding.clearEmailFieldButton.setOnClickListener {
            activitySigninBinding.emailInputField.text.clear()
        }

        activitySigninBinding.clearPasswordFieldButton.setOnClickListener {
            activitySigninBinding.passwordInputField.text.clear()
        }
    }

    private fun attemptLogin() {
        val email = activitySigninBinding.emailInputField.text.toString().trim()
        val password = activitySigninBinding.passwordInputField.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Both the email and password field must not be empty",
                Toast.LENGTH_LONG)
                .show()
            return
        }

        firebaseAuthentication.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {signInTask ->
                if (signInTask.isSuccessful) {
                    val firebaseUser = firebaseAuthentication.currentUser
                    redirectToMainActivity(firebaseUser)
                } else {
                    Toast.makeText(
                        this,
                        "Your user account could not be found. Please try again",
                        Toast.LENGTH_LONG)
                        .show()
                    return@addOnCompleteListener
                }
            }
    }

    private fun redirectToMainActivity(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        }
    }
}