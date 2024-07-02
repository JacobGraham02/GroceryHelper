package com.jacobdamiangraham.groceryhelper.ui.forgotpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.ActivityForgotPasswordBinding
import com.jacobdamiangraham.groceryhelper.enums.ForgotPasswordEmailType
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil

class ForgotPasswordView : AppCompatActivity() {

    private lateinit var activityForgotPasswordBinding: ActivityForgotPasswordBinding
    private lateinit var emailEditText: EditText
    private lateinit var sendEmailButton: Button
    private lateinit var emailInputLabel: TextView
    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(activityForgotPasswordBinding.root)
        emailEditText = activityForgotPasswordBinding.submitEmailEditText
        sendEmailButton = activityForgotPasswordBinding.forgotPasswordSendEmailButton
        emailInputLabel = activityForgotPasswordBinding.emailInputLabel
        setupPasswordResetEmailButton()
        setupEmailInputValidationListener()
    }

    private fun setupEmailInputValidationListener() {
        emailEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(ForgotPasswordEmailType.EMAIL, s.toString())
            }
        })
    }

    private fun validate(forgotPasswordEmailType: ForgotPasswordEmailType, value: String) {
        when (forgotPasswordEmailType) {
            ForgotPasswordEmailType.EMAIL -> {
                if (ValidationUtil.isValidEmailAddress(value)) {
                    emailEditText.setBackgroundResource(R.drawable.edit_text_valid)
                    emailInputLabel.text = getString(R.string.valid_email)
                    emailInputLabel.setTextColor(ContextCompat.getColor(this, R.color.green))
                    emailEditText.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    emailEditText.setBackgroundResource(R.drawable.edit_text_invalid)
                    emailInputLabel.text = getString(R.string.invalid_email)
                    emailInputLabel.setTextColor(ContextCompat.getColor(this, R.color.red))
                    emailEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }
        }
    }

    private fun setupPasswordResetEmailButton() {
        sendEmailButton.setOnClickListener {
            val userEmail = emailEditText.text.toString().trim()
            Log.w("PasswordResetEmail", userEmail)
            if (userEmail.isNotBlank()) {
                firebaseStorage.sendPasswordResetEmail(userEmail) {
                    success, message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }
    }
}