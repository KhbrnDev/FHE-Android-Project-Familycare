package de.fhe.familycare.storage.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.fhe.familycare.storage.enums.Gender

/**
 * Entity that represents a FamilyMember
 */
@Entity
data class FamilyMember(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @NonNull
    var name: String = "",

    @NonNull
    var isCastrated: Boolean = false,

    @NonNull
    var isHuman: Boolean = false,

    @NonNull
    var birthdate: String? = "",

    @NonNull
    var gender: Gender? = null,

    @NonNull
    var isActive: Boolean = true,

    var picturePath: String? = null,

    var note: String = "",

    @NonNull
    var type: FamilyMemberType? = null,

    var height: Int = 1

) {

    /**
     * Function to get a String for the Birthday in the right format.
     *
     * @param year: The year of birth a Int
     * @param month: The month of birth as Int
     * @param day: The day of birth as Int
     * @return A String in the Format dd.MM.yyyy
     */
    fun formatBirthdate(year: Int, month: Int, day: Int) : String{
        var savedDay = ""
        var savedMonth = ""
        var savedYear = ""

        savedDay = if(day > 9) {
            day.toString()
        } else {
            "0$day"
        }
        savedMonth = if((month + 1) > 9) {
            (month + 1).toString()
        } else {
            "0" + (month + 1).toString()
        }

        savedYear = year.toString()

        return "$savedDay.$savedMonth.$savedYear"
    }
}
