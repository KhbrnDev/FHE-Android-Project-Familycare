package de.fhe.familycare.view.core

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * BaseFragment for unique hideKeyboard function
 */
open class BaseFragment : Fragment() {

    protected fun <T : ViewModel?> getViewModel(tClass: Class<T>): T{
        return ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(
                requireActivity().application
            )
        ).get(tClass)
    }

    fun hideKeyboard(context: Context, view: View){
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}