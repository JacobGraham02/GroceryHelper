package com.jacobdamiangraham.groceryhelper.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.ActivityRegisterBinding
import com.jacobdamiangraham.groceryhelper.enums.InputType
import com.jacobdamiangraham.groceryhelper.interfaces.IUserRegistrationCallback
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil

class RegisterView: AppCompatActivity() {

    private lateinit var activityRegisterBinding: ActivityRegisterBinding
    private val firebaseStorage: FirebaseStorage = FirebaseStorage("users")

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

        activityRegisterBinding.emailInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validate(InputType.EMAIL, s.toString())
            }
        })

        activityRegisterBinding.passwordInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validate(InputType.PASSWORD, s.toString())
            }
        })

        activityRegisterBinding.confirmPasswordInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validate(InputType.CONFIRM_PASSWORD, s.toString(), activityRegisterBinding.passwordInputField.text.toString())
            }
        })
    }

    private fun validate(inputType: InputType, value: String, confirmPassword: String? = null) {
        when (inputType) {
            InputType.EMAIL -> {
                if (ValidationUtil.isValidEmailAddress(value)) {
                    activityRegisterBinding.emailInputField.setBackgroundResource(R.drawable.edit_text_valid)
                    activityRegisterBinding.emailInputTextView.text = getString(R.string.valid_email)
                    activityRegisterBinding.emailInputTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
                    activityRegisterBinding.emailInputField.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    activityRegisterBinding.emailInputField.setBackgroundResource(R.drawable.edit_text_invalid)
                    activityRegisterBinding.emailInputTextView.text = getString(R.string.invalid_email)
                    activityRegisterBinding.emailInputTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
                    activityRegisterBinding.emailInputField.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }

            InputType.PASSWORD -> {
                if (ValidationUtil.isValidPassword(value)) {
                    activityRegisterBinding.passwordInputField.setBackgroundResource(R.drawable.edit_text_valid)
                    activityRegisterBinding.passwordInputTextView.text = getString(R.string.valid_password)
                    activityRegisterBinding.passwordInputTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
                    activityRegisterBinding.passwordInputField.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    activityRegisterBinding.passwordInputField.setBackgroundResource(R.drawable.edit_text_invalid)
                    activityRegisterBinding.passwordInputTextView.text = getString(R.string.invalid_password)
                    activityRegisterBinding.passwordInputTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
                    activityRegisterBinding.passwordInputField.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }

            InputType.CONFIRM_PASSWORD -> {
                if (value == confirmPassword) {
                    activityRegisterBinding.confirmPasswordInputField.setBackgroundResource(R.drawable.edit_text_valid)
                    activityRegisterBinding.confirmPasswordInputTextView.text = getString(R.string.valid_confirm_password)
                    activityRegisterBinding.confirmPasswordInputTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
                    activityRegisterBinding.confirmPasswordInputField.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    activityRegisterBinding.confirmPasswordInputField.setBackgroundResource(R.drawable.edit_text_invalid)
                    activityRegisterBinding.confirmPasswordInputTextView.text = getString(R.string.invalid_confirm_password)
                    activityRegisterBinding.confirmPasswordInputTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
                    activityRegisterBinding.confirmPasswordInputField.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }
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
            return
        }

        firebaseStorage.registerUserInFirebase(email, password, object : IUserRegistrationCallback {
            override fun onRegistrationSuccess(successMessage: String) {
                Toast.makeText(
                    this@RegisterView,
                    successMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
                backToLoginActivity()
            }

            override fun onRegistrationFailure(errorMessage: String) {
                Toast.makeText(
                    this@RegisterView,
                    errorMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        })
    }

    private fun backToLoginActivity() {
        val signInActivityIntent = Intent(this, SignInView::class.java)
        startActivity(signInActivityIntent)
        finish()
    }
}