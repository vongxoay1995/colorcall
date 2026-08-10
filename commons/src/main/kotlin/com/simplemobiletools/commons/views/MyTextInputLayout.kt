package com.simplemobiletools.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.commons.helpers.HIGHER_ALPHA
import com.simplemobiletools.commons.helpers.MEDIUM_ALPHA

class MyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        val input = editText ?: return
        input.setTextColor(textColor)
        input.backgroundTintList = ColorStateList.valueOf(accentColor)

        val hintColor = if (input.value.isEmpty()) textColor.adjustAlpha(HIGHER_ALPHA) else textColor
        setHintTextColor(ColorStateList.valueOf(accentColor))

        val defaultHintTextColor = textColor.adjustAlpha(MEDIUM_ALPHA)
        val boxColorState = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_active),
                intArrayOf(android.R.attr.state_focused)
            ),
            intArrayOf(
                defaultHintTextColor,
                accentColor
            )
        )

        setEndIconTintList(ColorStateList.valueOf(hintColor))
        setBoxStrokeColorStateList(boxColorState)
        setDefaultHintTextColor(ColorStateList.valueOf(defaultHintTextColor))
        setHelperTextColor(ColorStateList.valueOf(textColor))
    }
}
