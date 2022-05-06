package de.fhe.familycare.view.core

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

/**
 * used to validate user input and show error messages
 * function [validate] needs to be implemented with error detection and handling 
 */
abstract class TextValidator(var textView: TextView): TextWatcher {

    abstract fun validate(textView: TextView, text: String)

    override fun afterTextChanged(s: Editable?) {
        val text = textView.text.toString()
        validate(textView, text)
    }

    override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        s: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) {
    }
}