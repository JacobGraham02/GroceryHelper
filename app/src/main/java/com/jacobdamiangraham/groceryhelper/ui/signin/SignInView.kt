package com.jacobdamiangraham.groceryhelper.ui.signin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jacobdamiangraham.groceryhelper.MainActivity
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.ActivitySigninBinding
import com.jacobdamiangraham.groceryhelper.enums.InputType
import com.jacobdamiangraham.groceryhelper.ui.register.RegisterView
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil

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

        activitySignInBinding.emailInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validate(InputType.EMAIL, s.toString())
            }
        })

        activitySignInBinding.passwordInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validate(InputType.PASSWORD, s.toString())
            }
        })
    }

    private fun validate(inputType: InputType, value: String) {
        when (inputType) {
            InputType.EMAIL -> {
                if (ValidationUtil.isValidEmailAddress(value)) {
                    activitySignInBinding.emailInputField.setBackgroundResource(R.drawable.edit_text_valid)
                    activitySignInBinding.emailInputTextView.text = getString(R.string.valid_email)
                    activitySignInBinding.emailInputTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
                    activitySignInBinding.emailInputField.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    activitySignInBinding.emailInputField.setBackgroundResource(R.drawable.edit_text_invalid)
                    activitySignInBinding.emailInputTextView.text = getString(R.string.invalid_email)
                    activitySignInBinding.emailInputTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
                    activitySignInBinding.emailInputField.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }

            InputType.PASSWORD -> {
                if (ValidationUtil.isValidPassword(value)) {
                    activitySignInBinding.passwordInputField.setBackgroundResource(R.drawable.edit_text_valid)
                    activitySignInBinding.passwordInputTextView.text = getString(R.string.valid_password)
                    activitySignInBinding.passwordInputTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
                    activitySignInBinding.passwordInputField.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    activitySignInBinding.passwordInputField.setBackgroundResource(R.drawable.edit_text_invalid)
                    activitySignInBinding.passwordInputTextView.text = getString(R.string.invalid_password)
                    activitySignInBinding.passwordInputTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
                    activitySignInBinding.passwordInputField.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }
            InputType.CONFIRM_PASSWORD -> {}
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

        val validEmail = ValidationUtil.isValidEmailAddress(email)
        val validPassword = ValidationUtil.isValidPassword(password)

        if (!validEmail || !validPassword) {
            Toast.makeText(
                this,
                "Please enter a valid email and password",
                Toast.LENGTH_LONG)
                .show()
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