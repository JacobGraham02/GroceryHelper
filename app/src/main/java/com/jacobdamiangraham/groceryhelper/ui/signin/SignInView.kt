package com.jacobdamiangraham.groceryhelper.ui.signin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jacobdamiangraham.groceryhelper.MainActivity
import com.jacobdamiangraham.groceryhelper.databinding.ActivitySigninBinding
import com.jacobdamiangraham.groceryhelper.ui.register.RegisterView

class SignInView : AppCompatActivity() {

    private lateinit var activitySignInBinding: ActivitySigninBinding
    private lateinit var firebaseAuthentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignInBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(activitySignInBinding.root)

        firebaseAuthentication = FirebaseAuth.getInstance()

        activitySignInBinding.signInButton.setOnClickListener {
            attemptLogin()
        }

        activitySignInBinding.clearEmailFieldButton.setOnClickListener {
            activitySignInBinding.emailInputField.text.clear()
        }

        activitySignInBinding.clearPasswordFieldButton.setOnClickListener {
            activitySignInBinding.passwordInputField.text.clear()
        }

        activitySignInBinding.registerAccountButton.setOnClickListener {
            redirectToRegisterActivity()
        }
    }

    private fun attemptLogin() {
        val email = activitySignInBinding.emailInputField.text.toString().trim()
        val password = activitySignInBinding.passwordInputField.text.toString().trim()

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

    private fun redirectToRegisterActivity() {
        val registerActivityIntent = Intent(this, RegisterView::class.java)
        startActivity(registerActivityIntent)
        finish()
    }
}