package de.fhe.familycare.storage.core

import android.util.Log
import androidx.room.TypeConverter
import de.fhe.familycare.storage.enums.Gender
import de.fhe.familycare.storage.model.FamilyMemberType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Class for type conversion for room Database.
 */
class Converters {

    /**
     * Converts String to Gender. Is needed to get Gender of FamilyMember from saved String.
     *
     * @return The Gender-object corresponding to the saved string
     */
    @TypeConverter
    fun stringToGender(value: String?): Gender {
        Log.i("Converter", "String: ${value!!}")
        val gender : Gender = Gender.valueOf(value!!)
        Log.i("Converter", "gender.toString(): ${gender.toString()}")

        return gender
    }

    /**
     * Converts Gender to String. Is needed to save Gender of FamilyMember in Database
     *
     * @return The String-equivalent of the Gender to be saved in the database
     */
    @TypeConverter
    fun genderToString(gender: Gender?): String? {
        return gender?.toString()
    }

    /**
     * Converts Timestamp to Date. Is needed to get Date of WeightData from saved Timestamp
     *
     * @return The Date object corresponding to the saved timestamp
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts Date to timestamp as Long. Is needed to save Date of WeightData in Database
     *
     * @return The timestamp of the Date as Long to be saved in the database
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    /**
     * Converts String to FamilyMemberType.
     * Is needed to get FamilyMemberType of FamilyMember from saved String
     *
     * @return The FamilyMemberType object corresponding to the saved String
     */
    @TypeConverter
    fun stringToFamilyMemberType(value: String?): FamilyMemberType? {
        return value?.let {FamilyMemberType (value)}
    }

    /**
     * Converts FamilyMemberType to String.
     * Is needed to save FamilyMemberType of FamilyMember in Database
     *
     * @return The String-equivalent of the FamilyMemberType to be saved in the database
     */
    @TypeConverter
    fun familyMemberTypeToString(familyMemberType: FamilyMemberType?): String?{
        return familyMemberType?.name.toString()
    }

    /**
     * Converts LocalDateTime to String. Is needed to save LocalDateTime of Appointment in Database
     *
     * @return The Long-equivalent in Seconds of the LocalDateTime to be saved in the database
     */
    @TypeConverter
    fun localDateTimeToLong(date: LocalDateTime?): Long?{
        val zonedDateTime = ZonedDateTime.of(date, ZoneId.systemDefault())
        Log.i("FM-Converters", "date = $date | seconds = ${zonedDateTime.toEpochSecond()}")
        return zonedDateTime.toEpochSecond()
    }

    /**
     * Converts String to LocalDateTime.
     * Is needed to get LocalDateTime of Appointment from saved String
     *
     * @return The LocalDateTime object corresponding to the saved Long in Seconds
     */
    @TypeConverter
    fun longToLocalDateTime(value: Long?): LocalDateTime? {
        val dateMillis = value?.let { Instant.ofEpochSecond(it) }
        val timeZone = TimeZone.getDefault().toZoneId()
        return LocalDateTime.ofInstant(dateMillis, timeZone)
    }
}