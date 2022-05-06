package de.fhe.familycare.storage.model

import android.util.Log
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Entity that represents an appointment
 */
@Entity
data class Appointment(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @NonNull
    var title: String = "",

    @NonNull
    var date_start: LocalDateTime? = null,

    var familyMemberID: Long? = null,

    @Ignore
    var familyMemberName: String? = "",

    var note: String = ""

) {
    /**
     * function to get a String from the start date of the appointment
     *
     * @return The String representation of the start date
     */
    fun formatStartDate() : String{
        return formatDateTime(this.date_start!!)
    }

    companion object {

        /**
         * function to format a LocalDateTime object to String in a specific way
         *
         * @return a String in the format dd.MM.yyyy HH:mm
         */
        fun formatDateTime(dateTime: LocalDateTime): String {
            Log.i("FM-Model", dateTime.toString())
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            return dateTime.format(formatter)
        }

        /**
         * function to parse a String in the format dd.MM.yyyy HH:mm to a LocalDateTime Object
         *
         * @param dateString a String in the format dd.MM.yyyy HH:mm
         * @return a LocalDateTime object corresponding to the given String
         */
        fun parseDate(dateString: String): LocalDateTime{
            val germanFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            val germanFormattedLocalTime =  LocalDateTime.parse(dateString, germanFormatter)!!
            val ldtFormatter = LocalDateTime.parse(germanFormattedLocalTime.toString())
            return ldtFormatter
        }
    }
}