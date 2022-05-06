package de.fhe.familycare.storage.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that represents a FamilyMemberType.
 * Is implemented as model and not as enum to make dynamic adding of FamilyMemberTypes possible.
 */
@Entity
class FamilyMemberType(
    @NonNull
    @PrimaryKey
    var name: String = "",
)