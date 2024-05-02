package com.jacobdamiangraham.groceryhelper.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog.Builder
import com.jacobdamiangraham.groceryhelper.interfaces.IAlertDialogGenerator
import com.jacobdamiangraham.groceryhelper.model.DialogInformation

class PromptBuilder(private val type: String): IAlertDialogGenerator {

    override fun configure(
        context: Context,
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation
    ): Builder {
        return when (type) {
            "error" -> configureErrorDialog(context, alertDialogBuilder, dialogInformation)
            "confirmation" -> configureConfirmationDialog(context, alertDialogBuilder, dialogInformation)
            "info" -> configureInfoDialog(context, alertDialogBuilder, dialogInformation)
            else -> throw IllegalArgumentException("No valid dialog box type could be found")
        }
    }

    private fun configureErrorDialog(context: Context, alertDialogBuilder: Builder, dialogInformation: DialogInformation): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton("Delete") { dialog, id ->

            }
            setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }
        }
    }

    private fun configureConfirmationDialog(context: Context, alertDialogBuilder: Builder, dialogInformation: DialogInformation): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton("Yes") { dialog, id ->

            }
            setNegativeButton("No") { dialog, id ->
                dialog.cancel()
            }
        }
    }

    private fun configureInfoDialog(context: Context, alertDialogBuilder: Builder, dialogInformation: DialogInformation): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setNegativeButton("Ok") { dialog, id ->
                dialog.cancel()
            }
        }
    }
}