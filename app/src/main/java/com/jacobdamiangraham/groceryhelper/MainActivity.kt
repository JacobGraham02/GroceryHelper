package com.jacobdamiangraham.groceryhelper

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.jacobdamiangraham.groceryhelper.databinding.ActivityMainBinding
import com.jacobdamiangraham.groceryhelper.factory.GroceryViewModelFactory
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLogoutCallback
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
import com.jacobdamiangraham.groceryhelper.notification.NotificationBuilder
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView
import com.jacobdamiangraham.groceryhelper.viewmodel.GroceryViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationBuilder: NotificationBuilder
    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    private lateinit var viewModel: GroceryViewModel
    private lateinit var viewModelFactory: GroceryViewModelFactory

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

        viewModelFactory = GroceryViewModelFactory("food basics")
        viewModel = ViewModelProvider(this).get(GroceryViewModel::class.java)

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
        setSupportActionBar(binding.appBarMain.toolbar)

        loadAndUpdateStoreNames()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_add_grocery_item -> {
                    navController.navigate(R.id.nav_add_grocery_item)
                }
                R.id.nav_log_out_icon -> {
                    val dialogInfo = DialogInformation(
                        title = "Confirm log out",
                        message = "Are you sure you want to log out of your account?"
                    )
                    val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                        "confirmation"
                    )
                    alertDialogGenerator.configure(
                        this,
                        AlertDialog.Builder(this),
                        dialogInfo,
                        positiveButtonAction = {
                            logOutOfAccount()
                        }
                    ).show()
                }
                R.id.nav_delete_account -> {
                    val dialogInfo = DialogInformation(
                        title = "Confirm delete account",
                        message = "Are you sure you want to delete your account?"
                    )
                    val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                        "confirmation"
                    )
                    alertDialogGenerator.configure(
                        this,
                        AlertDialog.Builder(this),
                        dialogInfo,
                        positiveButtonAction = {
                            deleteFirebaseUserAccount()
                        }
                    ).show()
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
    }

    private fun loadAndUpdateStoreNames() {
        firebaseStorage.getGroceryStoreNames { storeNames ->
            if (storeNames.isNotEmpty()) {
                updateNavigationMenu(storeNames)
                Toast.makeText(this, "Welcome to Grocery Helper!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No stores available to display.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateNavigationMenu(storeList: List<String>) {
        val menu = binding.navView.menu
        val storeGroup = menu.addSubMenu("Stores")

        storeList.forEach {
            storeName ->
                storeGroup.add(R.id.nav_home, Menu.NONE, Menu.NONE, storeName)
                    .setIcon(R.drawable.home_icon)
                    .setOnMenuItemClickListener {
                        val bundle = bundleOf("storeName" to storeName)
                        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_home, bundle)
                        binding.drawerLayout.closeDrawers()
                        true
                    }
        }

        binding.navView.invalidate()
    }

    private fun deleteFirebaseUserAccount() {
        firebaseStorage.deleteUserAccount(object : IUserLogoutCallback {
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

    private fun logOutOfAccount() {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_a_to_z -> viewModel.sortByNameAToZ()
            R.id.sort_by_alphabetical_category -> viewModel.sortByCategoryAToZ()
            R.id.sort_cost_high_to_low -> viewModel.sortByCostHighToLow()
            R.id.sort_cost_low_to_high -> viewModel.sortByCostLowToHigh()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}