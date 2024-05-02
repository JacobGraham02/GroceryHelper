package com.jacobdamiangraham.groceryhelper.factory

import com.jacobdamiangraham.groceryhelper.interfaces.IAlertDialogGenerator
import com.jacobdamiangraham.groceryhelper.utils.PromptBuilder

object PromptBuilderFactory {

    fun getAlertDialogGenerator(type: String): IAlertDialogGenerator {
        return when (type) {
            "delete" -> PromptBuilder()
            else ->
                throw IllegalArgumentException("Unknown dialog box generator type")
        }
    }
}