package com.jacobdamiangraham.groceryhelper.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog.Builder
import com.jacobdamiangraham.groceryhelper.model.DialogInformation

class PromptBuilder(private val type: String) {

    fun configure(
        context: Context,
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation,
        positiveButtonAction: (() -> Unit)? = null
    ): Builder {
        return when (type) {
            "error" -> configureErrorDialog(context, alertDialogBuilder, dialogInformation, positiveButtonAction)
            "confirmation" -> configureConfirmationDialog(context, alertDialogBuilder, dialogInformation, positiveButtonAction)
            "info" -> configureInfoDialog(context, alertDialogBuilder, dialogInformation)
            else -> throw IllegalArgumentException("No valid dialog box type could be found")
        }
    }

    private fun configureErrorDialog(context: Context, alertDialogBuilder: Builder, dialogInformation: DialogInformation, positiveButtonAction: (() -> Unit)?): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton("Delete") { dialog, id ->
                positiveButtonAction?.invoke()
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }
        }
    }

    private fun configureConfirmationDialog(context: Context, alertDialogBuilder: Builder, dialogInformation: DialogInformation, positiveButtonAction: (() -> Unit)?): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton("Yes") { dialog, id ->
                positiveButtonAction?.invoke()
                dialog.dismiss()
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