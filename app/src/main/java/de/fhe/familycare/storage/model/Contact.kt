package de.fhe.familycare.storage.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that represents a contact
 */
@Entity
data class Contact(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @NonNull
    var name: String = "",

    var phone: String = "",

    var email: String = "",

    var note: String = ""

)