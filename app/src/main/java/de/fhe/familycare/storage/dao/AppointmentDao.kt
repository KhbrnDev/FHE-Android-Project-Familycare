package de.fhe.familycare.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.fhe.familycare.storage.model.Appointment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AppointmentDao {

    @Query("DELETE FROM APPOINTMENT")
    fun deleteAll()

    @Query("SELECT * FROM Appointment ORDER BY date_start ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUpdateAppointment(appointment: Appointment) : Long

    @Query("SELECT * FROM Appointment WHERE id = :appointmentId")
    fun getAppointmentByID(appointmentId: Long): Flow<Appointment>

    @Query("SELECT * FROM Appointment WHERE familyMemberID = :familyMemberId ORDER BY date_start ASC")
    fun getAppointmentsOfFamilyMember(familyMemberId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM Appointment WHERE date_start > :date")
    fun getAllFutureAppointments(date: LocalDateTime): Flow<List<Appointment>>

    @Query("SELECT *  FROM Appointment WHERE date_start > :date AND familyMemberID = :familyMemberId")
    fun getAllFutureAppointmentsOfFamilyMember(familyMemberId: Long, date: LocalDateTime): Flow<List<Appointment>>

    @Query("DELETE FROM Appointment WHERE id = :appointmentId")
    fun deleteAppointment(appointmentId: Long)
}