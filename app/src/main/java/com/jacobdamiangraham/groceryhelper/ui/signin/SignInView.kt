package com.jacobdamiangraham.groceryhelper.ui.signin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jacobdamiangraham.groceryhelper.MainActivity
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.ActivitySigninBinding
import com.jacobdamiangraham.groceryhelper.enums.SignInInputType
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLoginCallback
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.ui.register.RegisterView
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil

class SignInView : AppCompatActivity() {

    private lateinit var activitySignInBinding: ActivitySigninBinding
    private val firebaseStorage: FirebaseStorage = FirebaseStorage("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignInBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(activitySignInBinding.root)

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
                validate(SignInInputType.EMAIL, s.toString())
            }
        })

        activitySignInBinding.passwordInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validate(SignInInputType.PASSWORD, s.toString())
            }
        })
    }

    private fun validate(signInInputType: SignInInputType, value: String) {
        when (signInInputType) {
            SignInInputType.EMAIL -> {
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

            SignInInputType.PASSWORD -> {
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
            SignInInputType.CONFIRM_PASSWORD -> {}
        }
    }

    private fun attemptLogin() {
        val email = activitySignInBinding.emailInputField.text.toString().trim()
        val password = activitySignInBinding.passwordInputField.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Both the email and password field must not be empty",
                Toast.LENGTH_LONG
            )
                .show()
            return
        }

        val validEmail = ValidationUtil.isValidEmailAddress(email)
        val validPassword = ValidationUtil.isValidPassword(password)

        if (!validEmail || !validPassword) {
            Toast.makeText(
                this,
                "Please enter a valid email and password",
                Toast.LENGTH_LONG
            )
                .show()
        }

        firebaseStorage.logInUserWithFirebase(email, password, object : IUserLoginCallback {
            override fun onLoginSuccess(successMessage: String) {
                Toast.makeText(
                    this@SignInView,
                    successMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
                redirectToMainActivity()
            }

            override fun onLoginFailure(failureMessage: String) {
                Toast.makeText(
                    this@SignInView,
                    failureMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        })
    }

    private fun redirectToMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun redirectToRegisterActivity() {
        val registerActivityIntent = Intent(this, RegisterView::class.java)
        startActivity(registerActivityIntent)
        finish()
    }
}