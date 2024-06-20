package com.jacobdamiangraham.groceryhelper.ui.signin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.jacobdamiangraham.groceryhelper.MainActivity
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.ActivitySigninBinding
import com.jacobdamiangraham.groceryhelper.enums.SignInInputType
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLoginCallback
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.ui.forgotpassword.ForgotPasswordView
import com.jacobdamiangraham.groceryhelper.ui.register.RegisterView
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil

class SignInView : AppCompatActivity() {

    private lateinit var activitySignInBinding: ActivitySigninBinding
    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignInBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(activitySignInBinding.root)

        if (isUserLoggedIn(applicationContext)) {
            redirectToMainActivity()
        }

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

        activitySignInBinding.forgotPasswordButton.setOnClickListener {
            redirectToForgotPasswordActivity()
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
                Toast.LENGTH_SHORT
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
                Toast.LENGTH_SHORT
            )
                .show()
        }

        firebaseStorage.logInUserWithFirebase(email, password, applicationContext, object : IUserLoginCallback {
            override fun onLoginSuccess(successMessage: String) {
                Toast.makeText(
                    this@SignInView,
                    successMessage,
                    Toast.LENGTH_SHORT
                )
                    .show()
                redirectToMainActivity()
            }

            override fun onLoginFailure(failureMessage: String) {
                Toast.makeText(
                    this@SignInView,
                    failureMessage,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            override fun onVerifyEmailFail(failureMessage: String) {
                val dialogInfo = DialogInformation(
                    title = "Verify account",
                    message = "Please verify your account before logging in. If you need the email re-sent, please click the 'resend' button below"
                )
                val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                    "resend_email"
                )
                alertDialogGenerator.configure(
                    AlertDialog.Builder(this@SignInView),
                    dialogInfo,
                    positiveButtonAction = {
                        firebaseStorage.resendVerificationEmail { success, message ->
                            if (success) {
                                Toast.makeText(this@SignInView, "Verification email sent to your inbox", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SignInView, "Failed to resend verification email to your inbox", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ).show()
            }
        })
    }

    private fun isUserLoggedIn(context: Context): Boolean {
        return try {
            val masterKeyAlias = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "grocery_helper_shared_preferences",
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val signInToken = sharedPreferences.getString("grocery_helper_user_token", null)
            signInToken != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun redirectToMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun redirectToRegisterActivity() {
        val registerActivityIntent = Intent(this, RegisterView::class.java)
        startActivity(registerActivityIntent)
    }

    private fun redirectToForgotPasswordActivity() {
        val forgotPasswordActivityIntent = Intent(this, ForgotPasswordView::class.java)
        startActivity(forgotPasswordActivityIntent)
    }
}