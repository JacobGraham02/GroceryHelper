package com.jacobdamiangraham.groceryhelper.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jacobdamiangraham.groceryhelper.databinding.ActivityRegisterBinding
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil

class RegisterView: AppCompatActivity() {

    private lateinit var firebaseAuthentication: FirebaseAuth
    private lateinit var activityRegisterBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)

        activityRegisterBinding.registerButton.setOnClickListener {
            registerAccount()
        }

        activityRegisterBinding.backToLoginButton.setOnClickListener {
            backToLoginActivity()
        }

        activityRegisterBinding.clearEmailFieldButton.setOnClickListener {
            activityRegisterBinding.emailInputField.text.clear()
        }

        activityRegisterBinding.clearPasswordFieldButton.setOnClickListener {
            activityRegisterBinding.passwordInputField.text.clear()
            activityRegisterBinding.confirmPasswordInputField.text.clear()
        }
    }

    private fun registerAccount() {
        val email = activityRegisterBinding.emailInputField.text.toString().trim()
        val password = activityRegisterBinding.passwordInputField.text.toString().trim()
        val confirmPassword = activityRegisterBinding.confirmPasswordInputField.text.toString().trim()

        if (password != confirmPassword) {
            Toast.makeText(
                this,
                "Password and confirm password do not match",
                Toast.LENGTH_LONG)
                .show()
        }

        val validEmail = ValidationUtil.isValidEmailAddress(email)
        val validPassword = ValidationUtil.isValidPassword(password)

        if (!validEmail || !validPassword) {
            Toast.makeText(
                this,
                "Please enter a valid email and password",
                Toast.LENGTH_LONG)
                .show()
        }

        firebaseAuthentication.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { completedCreateUserTask ->
                if (completedCreateUserTask.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Registration successful. Redirecting to login...",
                        Toast.LENGTH_LONG)
                        .show()
                    backToLoginActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed. Please try again",
                        Toast.LENGTH_LONG)
                        .show()
                    return@addOnCompleteListener
                }
            }
    }

    private fun backToLoginActivity() {
        val signInActivityIntent = Intent(this, SignInView::class.java)
        startActivity(signInActivityIntent)
        finish()
    }
}