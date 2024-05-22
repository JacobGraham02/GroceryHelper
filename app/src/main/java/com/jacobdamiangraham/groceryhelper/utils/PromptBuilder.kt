package com.jacobdamiangraham.groceryhelper.utils

import androidx.appcompat.app.AlertDialog.Builder
import com.jacobdamiangraham.groceryhelper.model.DialogInformation

class PromptBuilder(private val type: String) {

    fun configure(
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation,
        positiveButtonAction: (() -> Unit)? = null,
        negativeButtonAction: (() -> Unit)? = null,
        firstNavigationOption: String? = null,
        secondNavigationOption: String? = null
    ): Builder {
        return when (type) {
            "error" -> configureErrorDialog(
                alertDialogBuilder,
                dialogInformation,
                positiveButtonAction
            )
            "confirmation" -> configureConfirmationDialog(
                alertDialogBuilder,
                dialogInformation,
                positiveButtonAction
            )
            "info" -> configureInfoDialog(alertDialogBuilder, dialogInformation)
            "navigation" -> configureNavigationDialog(
                alertDialogBuilder,
                dialogInformation,
                firstNavigationOption,
                secondNavigationOption,
                positiveButtonAction,
                negativeButtonAction
            )
            else -> throw IllegalArgumentException("No valid dialog box type could be found")
        }
    }

    private fun configureErrorDialog(
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation,
        positiveButtonAction: (() -> Unit)?
    ): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton("Delete") { dialog, _ ->
                positiveButtonAction?.invoke()
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        }
    }

    private fun configureConfirmationDialog(
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation,
        positiveButtonAction: (() -> Unit)?
    ): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton("Yes") { dialog, _ ->
                positiveButtonAction?.invoke()
                dialog.dismiss()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        }
    }

    private fun configureNavigationDialog(
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation,
        firstNavigationOptionTitle: String?,
        secondNavigationOptionTitle: String?,
        positiveButtonAction: (() -> Unit)?,
        negativeActionButton: (() -> Unit)?
    ): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setPositiveButton(firstNavigationOptionTitle) { dialog, _ ->
                positiveButtonAction?.invoke()
                dialog.dismiss()
            }
            setNegativeButton(secondNavigationOptionTitle) { dialog, _ ->
                negativeActionButton?.invoke()
                dialog.dismiss()
            }
        }
    }

    private fun configureInfoDialog(
        alertDialogBuilder: Builder,
        dialogInformation: DialogInformation
    ): Builder {
        return alertDialogBuilder.apply {
            setCancelable(true)
            setTitle(dialogInformation.title)
            setMessage(dialogInformation.message)
            setNegativeButton("Ok") { dialog, _ ->
                dialog.cancel()
            }
        }
    }
}