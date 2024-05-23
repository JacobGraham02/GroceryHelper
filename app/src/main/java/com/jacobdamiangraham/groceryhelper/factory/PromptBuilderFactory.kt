package com.jacobdamiangraham.groceryhelper.factory

import com.jacobdamiangraham.groceryhelper.utils.PromptBuilder

object PromptBuilderFactory {

    fun getAlertDialogGenerator(type: String): PromptBuilder {
       return PromptBuilder(type)
    }
}