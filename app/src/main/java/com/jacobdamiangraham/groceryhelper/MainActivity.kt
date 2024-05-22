package com.jacobdamiangraham.groceryhelper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.jacobdamiangraham.groceryhelper.databinding.ActivityMainBinding
import com.jacobdamiangraham.groceryhelper.event.UserDeleteAccountEvent
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryStoreCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.interfaces.IDeleteGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLogoutCallback
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
import com.jacobdamiangraham.groceryhelper.notification.NotificationBuilder
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView
import com.jacobdamiangraham.groceryhelper.viewmodel.GroceryViewModel

class MainActivity : AppCompatActivity(), Observer<UserDeleteAccountEvent> {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationBuilder: NotificationBuilder
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var viewModel: GroceryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseStorage = FirebaseStorage()
        firebaseStorage.deleteAccountObserver.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

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

        refreshNavigationMenu()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                }
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
                            logOutOfAccount(this)
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
                            deleteFirebaseUserAccount(this)
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
                    Toast.LENGTH_SHORT
                ).show()
                redirectToSignInScreen()
            }
        })
    }

    private fun refreshNavigationMenu() {
        firebaseStorage.getGroceryStoreNames { storeNames ->
            clearStoreMenu()
            updateNavigationMenu(storeNames)
        }
    }

    private fun clearStoreMenu() {
        val navMenu = binding.navView.menu
        val storeGroup = navMenu.findItem(R.id.store_group)?.subMenu
        storeGroup?.clear()
        binding.navView.invalidate()
    }

    private fun showStoreDeleteConfirmationDialog(storeName: String) {
        val dialogInfo = DialogInformation(
            title = "Confirm remove store",
            message = "Are you sure you want to remove this store from your list?"
        )

        val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
            "confirmation"
        )

        alertDialogGenerator.configure(
            this,
            AlertDialog.Builder(this),
            dialogInfo,
            positiveButtonAction = {
                firebaseStorage.deleteAllGroceryItemsByStore(
                    storeName,
                    object : IDeleteGroceryItemCallback {
                        override fun onDeleteSuccess(successMessage: String) {
                            firebaseStorage.deleteGroceryStoreFromUser(storeName, object :
                                IAddGroceryStoreCallback {
                                override fun onAddStoreSuccess(successMessage: String) {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            successMessage,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        refreshNavigationMenu()
                                    }
                                }

                                override fun onAddStoreFailure(failureMessage: String) {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            failureMessage,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            })
                        }

                        override fun onDeleteFailure(failureMessage: String) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, failureMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            }).show()
    }

    private fun updateNavigationMenu(storeList: List<String>) {
        val navMenu = binding.navView.menu

        var storeGroup = navMenu.findItem(R.id.store_group)?.subMenu

        if (storeGroup == null) {
            storeGroup = navMenu.addSubMenu(Menu.NONE, R.id.store_group, Menu.NONE, "Stores")
        } else {
            storeGroup.clear()
        }

        storeList.forEach { storeName ->
            storeGroup?.add(R.id.store_group, Menu.NONE, Menu.NONE, storeName)
                ?.setIcon(R.drawable.home_icon)
                ?.setOnMenuItemClickListener {
                    showStoreOptionsDialog(storeName)
                    true
                }
        }

        binding.navView.invalidate()
    }

    private fun showStoreOptionsDialog(storeName: String) {
        val options = arrayOf("Go to store list", "Delete store")
        AlertDialog.Builder(this)
            .setTitle(storeName)
            .setItems(options) { dialog, whichOptionSelected ->
                when(whichOptionSelected) {
                    0 -> navigateToStore(storeName)
                    1 -> showStoreDeleteConfirmationDialog(storeName)
                }
            }.show()
    }

    private fun navigateToStore(storeName: String) {
        val bundle = bundleOf("storeName" to storeName)
        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_home, bundle)
        binding.drawerLayout.closeDrawers()
    }

    private fun removeStoreFromMenu(storeName: String) {
        val navMenu = binding.navView.menu
        val storeGroup = navMenu.findItem(R.id.store_group)?.subMenu

        storeGroup?.let {
            for (i in 0 until it.size()) {
                val menuItem = it.getItem(i)
                if (menuItem.title == storeName) {
                    it.removeItem(menuItem.itemId)
                    break
                }
            }
        }

        binding.navView.invalidate()
    }

    private fun deleteFirebaseUserAccount(context: Context) {
        firebaseStorage.deleteUserAccount(context)
    }

    private fun logOutOfAccount(context: Context) {
        firebaseStorage.logoutWithFirebase(context, object: IUserLogoutCallback {
            override fun onLogoutSuccess(successMessage: String) {
                Toast.makeText(
                    this@MainActivity,
                    successMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onLogoutFailure(failureMessage: String) {
                Toast.makeText(
                    this@MainActivity,
                    failureMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun redirectToSignInScreen() {
        val signInActivityIntent = Intent(this, SignInView::class.java)
        startActivity(signInActivityIntent)
        finish()
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

    override fun onChanged(value: UserDeleteAccountEvent) {
        Toast.makeText(this, value.message, Toast.LENGTH_SHORT).show()
    }
}