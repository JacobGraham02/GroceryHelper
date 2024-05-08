package com.jacobdamiangraham.groceryhelper.utils

object ValidationUtil {

    /**
     * ^                 start-of-string
     * (?=.*[0-9])       a digit must occur at least once
     * (?=.*[a-z])       a lower case letter must occur at least once
     * (?=.*[A-Z])       an upper case letter must occur at least once
     * (?=.*[@#$%^&+=])  a special character must occur at least once you can replace with your special characters
     * (?=\\S+$)         no whitespace allowed in the entire string
     * .{4,}             anything, at least six places though
     * $                 end-of-string
     */
    private const val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$"

    fun isValidEmailAddress(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.matches(passwordRegex.toRegex())
    }

    fun isValidGroceryItemString(value: String): Boolean {
        return value.isNotBlank()
    }

    fun isValidQuantity(quantity: Int): Boolean {
        return quantity >= 1
    }

    fun isValidCost(cost: Float): Boolean {
        return cost >= 0.01
    }

    fun validateGroceryItemInputs(itemName: String, quantity: Int, category: String, store: String, cost: Float): Boolean {
        return  isValidGroceryItemString(itemName) &&
                isValidGroceryItemString(category) &&
                isValidGroceryItemString(store) &&
                isValidQuantity(quantity) &&
                isValidCost(cost)
    }
}