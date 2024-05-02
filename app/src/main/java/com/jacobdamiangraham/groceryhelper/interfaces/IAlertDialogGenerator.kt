package com.jacobdamiangraham.groceryhelper.interfaces

import android.content.Context
import androidx.appcompat.app.AlertDialog

interface IAlertDialogGenerator {

    fun configure(context: Context, alertDialogBuilder: AlertDialog.Builder): AlertDialog.Builder
}