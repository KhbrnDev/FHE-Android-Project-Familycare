package de.fhe.familycare.storage.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * Entity that represents WeightData.
 * The height and age of the FamilyMember is saved additionally to the FamilyMemberId because the
 * calculation of the bmi should be based on the age and height at the time that the FamilyMembers
 * weight is noted.
 */
@Entity
data class WeightData(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,

        @NotNull
        var weight: Float = 0.0f,

        @NotNull
        var familyMemberId: Long = 0,

        @NotNull
        var height: Float = 0.0f,

        @NotNull
        var age: Long = 0,

        @NotNull
        var date: Date? = null
)