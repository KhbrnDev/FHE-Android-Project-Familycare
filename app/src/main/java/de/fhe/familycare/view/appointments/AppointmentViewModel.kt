package de.fhe.familycare.view.appointments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.fhe.familycare.storage.model.Appointment
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.storage.repositories.AppointmentRepository
import de.fhe.familycare.storage.repositories.FamilyMemberRepository

/**
 * ViewModel for all Appointment-Related Views
 */
class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val _appointmentRepository = AppointmentRepository.getRepository(application)
    private val appointmentRepository get() = _appointmentRepository!!

    private val _familyMemberRepository = FamilyMemberRepository.getRepository(application)
    private val familyMemberRepository get() = _familyMemberRepository!!


    val allAppointments: LiveData<List<Appointment>> =
        appointmentRepository.getAllAppointments().asLiveData()

    val allFutureAppointments: LiveData<List<Appointment>> =
        appointmentRepository.getAllFutureAppointments().asLiveData()

    val allFamilyMembers: LiveData<List<FamilyMember>> =
        familyMemberRepository.getAllFamilyMember().asLiveData()

    fun saveAppointment(newAppointment: Appointment): Long {
        return appointmentRepository.insertUpdateAppointment(newAppointment)
    }

    fun getAppointment(id: Long): LiveData<Appointment> {
        return appointmentRepository.getAppointmentByID(id).asLiveData()
    }

    fun deleteAppointment(appointmentId: Long){
        appointmentRepository.deleteAppointment(appointmentId)
    }
}