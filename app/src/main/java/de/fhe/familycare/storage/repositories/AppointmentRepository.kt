package de.fhe.familycare.storage.repositories

import android.app.Application
import android.content.Context
import de.fhe.familycare.storage.core.FamilyCareDatabase
import de.fhe.familycare.storage.dao.AppointmentDao
import de.fhe.familycare.storage.dao.FamilyMemberDao
import de.fhe.familycare.storage.model.Appointment
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Repository for Appointments
 */
class AppointmentRepository(context: Context) {

    private var _appointmentDao: AppointmentDao? = null
    private val appointmentDao get() = _appointmentDao!!

    private var _familyMemberDao: FamilyMemberDao? = null
    private val familyMemberDao get() = _familyMemberDao!!

    init {
        val db : FamilyCareDatabase? = FamilyCareDatabase.getDatabase(context)
        _appointmentDao = db?.appointmentDao()
        _familyMemberDao = db?.familyMemberDao()
    }

    companion object {
        private var INSTANCE : AppointmentRepository? = null

        fun getRepository (application : Application) : AppointmentRepository?{
            if(INSTANCE == null) {
                synchronized(AppointmentRepository::class.java){
                    if (INSTANCE == null){
                        INSTANCE = AppointmentRepository(application)
                    }
                }
            }
            return INSTANCE
        }
    }

    /**
     * get all appointments and set FamilyMemberName for each appointment
     */
    fun getAllAppointments(): Flow<List<Appointment>> {

        val familyMembers = familyMemberDao.getAllFamilyMember()
        val appointments = appointmentDao.getAllAppointments()

         return appointments.combine(familyMembers){appointmentList, familyMemberList ->
            appointmentList.forEach { appointment ->
                appointment.familyMemberName = familyMemberList.find { familyMember ->
                    familyMember.id == appointment.familyMemberID
                }?.name
            }
            return@combine appointmentList
        }
    }

    /**
     * inserts or updates appointment in database
     * @param appointment: the appointment to be saved or updated
     * @return the ID of the new or updated Appointment. If something went wrong -1 is returned
     */
    fun insertUpdateAppointment(appointment: Appointment):Long{
        try {
            return FamilyCareDatabase.executeWithReturn { appointmentDao.insertUpdateAppointment(appointment) }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return -1
    }

    /**
     * Function to get the appointment of the given ID
     *
     * @param appointmentId: the ID of the appointment
     * @return The appointment as Flow
     */
    fun getAppointmentByID(appointmentId: Long): Flow<Appointment> {
        val appointment = appointmentDao.getAppointmentByID(appointmentId)
        return appointment.filterNotNull().map { _appointment ->
            _appointment.familyMemberName =
                _appointment.familyMemberID?.let { familyMemberDao.getFamilyMemberByID(it).first().name }
            return@map _appointment
        }
    }

    /**
     * Gets all appointments of given FamilyMember from database
     *
     * @param familyMemberId: The ID of the FamilyMember
     * @returns a Flow of a List of all Appointments of the FamilyMember
     */
    fun getAppointmentsOfFamilyMember(familyMemberId: Long): Flow<List<Appointment>> {
        val appointments = appointmentDao.getAppointmentsOfFamilyMember(familyMemberId)
        val familyMember = familyMemberDao.getFamilyMemberByID(familyMemberId)
        return appointments.combine(familyMember){_appointments, _familyMember ->
            _appointments.forEach{
                it.familyMemberName = _familyMember.name
            }
            return@combine _appointments
        }
    }

    /**
     * Gets all future Appointments
     *
     * @return a Flow of a List of all Future Appointments
     */
    fun getAllFutureAppointments() : Flow<List<Appointment>> {

        val ldtNow = LocalDateTime.now()
        val familyMembers = familyMemberDao.getAllFamilyMember()
        val appointments = appointmentDao.getAllFutureAppointments(ldtNow)

        return appointments.combine(familyMembers){appointmentList, familyMemberList ->
            appointmentList.forEach { appointment ->
                appointment.familyMemberName = familyMemberList.find { familyMember ->
                    familyMember.id == appointment.familyMemberID
                }?.name
            }
            return@combine appointmentList
        }
    }

    /**
     * Get all future Appointments of a FamilyMember
     *
     * @param familyMemberId: The ID of the FamilyMember
     * @return a Flow of a List of all future Appointments of a FamilyMember
     */
    fun getAllFutureAppointmentsOfFamilyMember(familyMemberId: Long): Flow<List<Appointment>> {

        val ldtNow = LocalDateTime.now()
        val appointments = appointmentDao.getAllFutureAppointmentsOfFamilyMember(familyMemberId, ldtNow)
        val familyMember = familyMemberDao.getFamilyMemberByID(familyMemberId)

        return appointments.combine(familyMember){_appointments, _familyMember ->
            _appointments.forEach{
                it.familyMemberName = _familyMember.name
            }
            return@combine _appointments
        }
    }

    /**
     * Delete Appointment by given Id
     *
     * @param appointmentId: the ID of the appointment
     */
    fun deleteAppointment(appointmentId: Long){
        try {
            FamilyCareDatabase.execute { appointmentDao.deleteAppointment(appointmentId) }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}