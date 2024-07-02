package com.jacobdamiangraham.groceryhelper.utils

object ValidationUtil {

    /**
     * ^                 start-of-string
     * (?=.*\d)         # Positive lookahead: ensure the string contains at least one digit (\d)
     * (?=.*[a-z])      # Positive lookahead: ensure the string contains at least one lowercase letter
     * (?=.*[A-Z])      # Positive lookahead: ensure the string contains at least one uppercase letter
     * .{8,}            # Match any character (except newline) at least 8 times
     * $                 end-of-string
     */
    private const val PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$"

    fun isValidEmailAddress(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.matches(PASSWORD_REGEX.toRegex())
    }

    fun isValidGroceryItemString(value: String): Boolean {
        return value.isNotBlank()
    }

    fun isValidQuantity(quantity: Int): Boolean {
        return quantity >= 1
    }

    fun isValidCost(cost: Float): Boolean {
        return cost >= 0.00
    }

    fun validateGroceryItemInputs(itemName: String, quantity: Int, category: String, store: String, cost: Float): Boolean {
        return  isValidGroceryItemString(itemName) &&
                isValidGroceryItemString(category) &&
                isValidGroceryItemString(store) &&
                isValidQuantity(quantity) &&
                isValidCost(cost)
    }
}