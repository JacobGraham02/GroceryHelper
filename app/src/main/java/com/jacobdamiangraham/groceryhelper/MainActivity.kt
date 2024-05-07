package com.jacobdamiangraham.groceryhelper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.jacobdamiangraham.groceryhelper.databinding.ActivityMainBinding
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLogoutCallback
import com.jacobdamiangraham.groceryhelper.notification.NotificationBuilder
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationBuilder: NotificationBuilder
    private val firebaseStorage: FirebaseStorage = FirebaseStorage("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setAnchorView(R.id.fab).show()
        }

        notificationBuilder = NotificationBuilder(this)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_add_grocery_item), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_food_basics_list -> {
                    val bundle = bundleOf("storeName" to "food basics")
                    navController.navigate(R.id.nav_home, bundle)
                }
                R.id.nav_zehrs_list -> {
                    val bundle = bundleOf("storeName" to "zehrs")
                    navController.navigate(R.id.nav_home, bundle)
                }
                R.id.nav_add_grocery_item -> {
                    navController.navigate(R.id.nav_add_grocery_item)
                }
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                }
                R.id.nav_log_out_icon -> {
                    firebaseStorage.logoutWithFirebase(object: IUserLogoutCallback {
                        override fun onLogoutSuccess(successMessage: String) {
                            Toast.makeText(
                                this@MainActivity,
                                successMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onLogoutFailure(failureMessage: String) {
                            Toast.makeText(
                                this@MainActivity,
                                failureMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    })
                }
            }
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }

        firebaseStorage.registerGlobalAuthenticationCheck(this, object : IAuthStatusListener {
            override fun onUserUnauthenticated(onUserUnauthenticatedMessage: String) {
                Toast.makeText(
                    this@MainActivity,
                    onUserUnauthenticatedMessage,
                    Toast.LENGTH_LONG
                ).show()
                redirectToSignInScreen()
            }
        })

        displayNotification("Test notification title", "Test notification description")
    }

    private fun redirectToSignInScreen() {
        val signInActivityIntent = Intent(this, SignInView::class.java)
        startActivity(signInActivityIntent)
        finish()
    }

    private fun displayNotification(title: String, description: String) {
        notificationBuilder.displayNotification(title, description)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}