package com.jacobdamiangraham.groceryhelper.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.jacobdamiangraham.groceryhelper.R

class CustomEditText(context: Context, attributes: AttributeSet) : AppCompatEditText(context, attributes) {

    var customTtsPrompt: String? = null

    init {
        context.theme.obtainStyledAttributes(attributes, R.styleable.CustomTtsAnnouncement, 0,0).apply {
            try {
                customTtsPrompt = getString(R.styleable.CustomTtsAnnouncement_TtsPrompt)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                recycle()
            }
        }
    }
}