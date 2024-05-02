package com.jacobdamiangraham.groceryhelper.interfaces

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.jacobdamiangraham.groceryhelper.model.DialogInformation

interface IAlertDialogGenerator {

    fun configure(context: Context,
                  alertDialogBuilder: AlertDialog.Builder,
                  dialogInformation: DialogInformation): AlertDialog.Builder
}