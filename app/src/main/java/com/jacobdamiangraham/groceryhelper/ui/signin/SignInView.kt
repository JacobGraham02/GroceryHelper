package com.jacobdamiangraham.groceryhelper.ui.signin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import com.jacobdamiangraham.groceryhelper.databinding.ActivitySigninBinding

class SignInView : AppCompatActivity() {

    private lateinit var activitySigninBinding: ActivitySigninBinding
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySigninBinding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(activitySigninBinding.root)
    }
}