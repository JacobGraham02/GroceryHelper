package com.jacobdamiangraham.groceryhelper.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.jacobdamiangraham.groceryhelper.interfaces.IAlertDialogGenerator

class PromptBuilder: IAlertDialogGenerator {

    override fun configure(context: Context, alertDialogBuilder: androidx.appcompat.app.AlertDialog.Builder): AlertDialog.Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle("Delete grocery item")
            setMessage("Are you sure you want to delete this grocery item")
            setPositiveButton("Delete") { dialog, id ->

            }
            setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }
        }
    }
}