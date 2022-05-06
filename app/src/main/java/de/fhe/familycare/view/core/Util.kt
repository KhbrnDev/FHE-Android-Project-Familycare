package de.fhe.familycare.view.core

import android.content.Intent
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar

class Util {

    companion object{

        fun makeSingleImplicitIntent(intent: Intent, view: View) : Boolean{

            if (intent.resolveActivity(view.context.packageManager) != null) {
                view.context.startActivity(intent)
                Log.i("FM_AllContactsAdapter", "No app to take action: ${intent.type.toString()}")
                return true
            } else {
                Log.i("FM_AllContactsAdapter", "No app to take action: ${intent.type.toString()}")
                Snackbar.make(view, "No app to take this action", Snackbar.LENGTH_SHORT).show()
                return false
            }
        }
    }
}