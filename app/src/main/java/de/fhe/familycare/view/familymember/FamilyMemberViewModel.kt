package de.fhe.familycare.view.familymember

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.fhe.familycare.storage.model.Appointment
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.storage.model.FamilyMemberType
import de.fhe.familycare.storage.model.WeightData
import de.fhe.familycare.storage.repositories.AppointmentRepository
import de.fhe.familycare.storage.repositories.FamilyMemberRepository
import de.fhe.familycare.storage.repositories.FamilyMemberTypeRepository
import de.fhe.familycare.storage.repositories.WeightDataRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * ViewModel for all FamilyMember related views
 */
class FamilyMemberViewModel(application: Application) : AndroidViewModel(application) {


    private val _weightDataRepository = WeightDataRepository.getRepository(application)
    private val weightDataRepository get() = _weightDataRepository!!

    private val _familyMemberRepository = FamilyMemberRepository.getRepository(application)
    private val familyMemberRepository get() = _familyMemberRepository!!

    private val _appointmentRepository = AppointmentRepository.getRepository(application)
    private val appointmentRepository get() = _appointmentRepository!!

    private val _familyMemberTypeRepository = FamilyMemberTypeRepository.getRepository(application)
    private val familyMemberTypeRepository get() = _familyMemberTypeRepository!!

    val allFamilyMemberTypes: LiveData<List<FamilyMemberType>> =
        familyMemberTypeRepository.getAllFamilyMemberTypes().asLiveData()
    val allFamilyMembers: LiveData<List<FamilyMember>> =
        familyMemberRepository.getAllFamilyMember().asLiveData()
    val allActiveFamilyMembers: LiveData<List<FamilyMember>> =
        familyMemberRepository.getAllActiveFamilyMembers().asLiveData()

    lateinit var familyMember: LiveData<FamilyMember>


    fun getAllWeightDataForFamilyMember(id: Long): LiveData<List<WeightData>> {
        return weightDataRepository.getAllWeightByFamilyMemberID(id).asLiveData()

    }

    fun getLastWeightDataByFamilyMemberId(familyMemberId: Long): LiveData<WeightData> {
        return weightDataRepository.getLastWeightByFamilyMemberID(familyMemberId).asLiveData()
    }

    fun getFamilyMember(id: Long): LiveData<FamilyMember> {

        familyMember = familyMemberRepository.getFamilyMemberByID(id).asLiveData()
        return familyMember
    }

    fun saveNewWeightData(weightData: WeightData) {
        weightDataRepository.insertUpdateWeightData(weightData)
    }

    fun saveFamilyMember(newFamilyMember: FamilyMember): Long {
        return familyMemberRepository.insertUpdateFamilyMember(newFamilyMember)
    }

    fun getAge(familyMember: FamilyMember): Long {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val birthdate: LocalDate = LocalDate.parse(familyMember.birthdate, formatter)
        val today: LocalDate = LocalDate.now()
        return ChronoUnit.YEARS.between(birthdate, today)
    }

    fun getAllAppointmentsOfFamilyMemberById(familyMemberId: Long): LiveData<List<Appointment>> {

        return appointmentRepository.getAppointmentsOfFamilyMember(familyMemberId).asLiveData()
    }

    fun getAllFutureAppointmentsOfFamilyMemberById(familyMemberId: Long): LiveData<List<Appointment>> {

        return appointmentRepository.getAllFutureAppointmentsOfFamilyMember(familyMemberId)
            .asLiveData()
    }

    fun saveNewFamilyMemberType(name: String): Long {
        return familyMemberTypeRepository.insertUpdateFamilyMemberType(name)
    }
}