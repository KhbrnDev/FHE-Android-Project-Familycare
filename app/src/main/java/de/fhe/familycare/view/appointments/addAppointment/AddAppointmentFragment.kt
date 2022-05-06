package de.fhe.familycare.view.appointments.addAppointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentAddAppointmentBinding
import de.fhe.familycare.storage.model.Appointment
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.view.appointments.AppointmentViewModel
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.TextValidator
import de.fhe.familycare.view.familymember.familyMemberViewPager.FamilyMemberViewPagerFragmentArgs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Fragment of AddAppointment View
 */
class AddAppointmentFragment : BaseFragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var navController: NavController

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!

    private var familyMemberList = mutableListOf<FamilyMember>()

    private lateinit var addAppointmentViewModel: AppointmentViewModel


    private val navigationArgs: FamilyMemberViewPagerFragmentArgs by navArgs()

    // Variables for the Date- and TimePickerDialog
    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0
    private var savedDay = ""
    private var savedMonth = ""
    private var savedYear = ""
    private var savedHour = ""
    private var savedMinute = ""

    /**
     * initialize ViewModel and NavController
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        addAppointmentViewModel = this.getViewModel(AppointmentViewModel::class.java)
        navController = findNavController()

        return binding.root
    }

    /**
     * populate Dropdown for FamilyMember selection
     * preload date and time to now
     * initiate input validation
     * sets OnClickListener to AddAppointment Button
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateSelectFamilyMemberDropdown()
        setDate()
        inputValidation()

        pickDate()

        binding.btnAddAppointment.setOnClickListener {

            val appointment = Appointment()
            appointment.title = binding.etTitle.editText?.text.toString()

            val tmpStartDate = binding.tvStartTimeText.text.toString()

            appointment.date_start = Appointment.parseDate(tmpStartDate)

            val familyMemberString = binding.actvFamilyMember.text.toString()
            appointment.familyMemberID = getIdOfFamilyMember(familyMemberString)

            appointment.note = binding.etNote.editText?.text.toString()

            addAppointmentViewModel.saveAppointment(appointment)

            super.hideKeyboard(this.requireContext(), this.requireView())

            navController.navigateUp()
        }

    }

    /**
     * creates an Calendar instance
     */
    private fun getDateTimeCalendar() {
        if(savedDay.isNullOrBlank() || savedMonth.isNullOrBlank() || savedYear.isNullOrBlank() || savedHour.isNullOrBlank() || savedMinute.isNullOrBlank()) {
            val cal: Calendar = Calendar.getInstance()
            day = cal.get(Calendar.DAY_OF_MONTH)
            month = cal.get(Calendar.MONTH)
            year = cal.get(Calendar.YEAR)
            hour = cal.get(Calendar.HOUR_OF_DAY)
            minute = cal.get(Calendar.MINUTE)
        } else {
            day = savedDay.toInt()
            month = savedMonth.toInt() -1
            year = savedYear.toInt()
            hour = savedHour.toInt()
            minute = savedMinute.toInt()
        }

    }

    /**
     * Sets OnClickListener To AddStartTime button and opens DatePickerDialog by clicking the button
     */
    private fun pickDate() {
        binding.buttonAddStartTime.setOnClickListener {

            getDateTimeCalendar()
            DatePickerDialog(requireContext(), this, year, month, day).show()
        }
    }

    /**
     * Function to load all existing FamilyMembers into the SelectFamilyMemberDropdown
     */
    private fun populateSelectFamilyMemberDropdown() {
        val familyMemberAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList<String>())
        binding.actvFamilyMember.setAdapter(familyMemberAdapter)
        familyMemberAdapter.add(getString(R.string.no_family_member))
        binding.actvFamilyMember.setText(getString(R.string.no_family_member), false)

        // get and set data
        addAppointmentViewModel.allFamilyMembers.observe(this.viewLifecycleOwner) { familyMembers ->
            familyMemberList = familyMembers.toMutableList()

            Log.i("FM", "typeSize = ${familyMembers.size}")

            familyMembers.forEach {
                Log.i("FM", "member = ${it.name}")
                familyMemberAdapter.add(it.name)
                if (navigationArgs.familyMemberID == it.id) {
                    binding.actvFamilyMember.setText(it.name, false)
                }
            }
            familyMemberAdapter.notifyDataSetChanged()
        }
    }

    /**
     * Function to get the id of a FamilyMember by its given name
     *
     * @param name: The name of the FamilyMember
     * @return The ID of the FamilyMember as Long
     */
    private fun getIdOfFamilyMember(name: String): Long? {
        return familyMemberList.find {
            it.name == name
        }?.id
    }

    /**
     * Function to initialize the selected Date to the time of opening the view.
     * Is used to make sure that there is always a valid date selected.
     */
    private fun setDate() {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val formattedNow = now.format(formatter)
        binding.tvStartTimeText.text = formattedNow
    }

    /**
     * Overrides function of DatePicker. Saves the selected Date in Variables
     * and opens the TimePickerDialog
     */
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = if (dayOfMonth > 9) {
            dayOfMonth.toString()
        } else {
            "0$dayOfMonth"
        }
        savedMonth = if ((month + 1) > 9) {
            (month + 1).toString()
        } else {
            "0" + (month + 1).toString()
        }

        savedYear = year.toString()

        TimePickerDialog(this.requireContext(), this, hour, minute, true).show()
    }

    /**
     * Overrides function of TimePicker.
     * saves the selected time and puts the selected date and time to the corresponding TextView
     */
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = if (hourOfDay > 9) {
            hourOfDay.toString()
        } else {
            "0$hourOfDay"
        }
        savedMinute = if (minute > 9) {
            minute.toString()
        } else {
            "0$minute"
        }

        binding.tvStartTimeText.text = getString(R.string.appointment_date_time_formatted, savedDay,savedMonth,savedYear,savedHour,savedMinute)
    }

    /**
     * Checks all user input fields for valid input
     */
    private fun inputValidation() {

        // initial setup
        binding.etTitle.error = getString(R.string.title_not_blank)
        binding.btnAddAppointment.isEnabled = false

        // validate Title not blank
        val etTitle = binding.etTitle.editText
        etTitle?.addTextChangedListener(object : TextValidator(etTitle) {
            override fun validate(textView: TextView, text: String) {
                if (text.isBlank()) {
                    binding.etTitle.error = getString(R.string.title_not_blank)
                    Log.i("FM", "error detected")
                    binding.btnAddAppointment.isEnabled = false
                    return
                }
                binding.etTitle.error = ""
                checkSubmitButton()
            }
        })

        // validate Note not longer than 255 characters
        val etNote = binding.etNote.editText
        etNote?.addTextChangedListener(object : TextValidator(etNote) {
            override fun validate(textView: TextView, text: String) {
                if (text.length > 255) {
                    binding.etNote.error = getString(R.string.note_too_long)
                    Log.i("FM", "error detected")
                    binding.btnAddAppointment.isEnabled = false
                    return
                }
                binding.etNote.error = ""
                checkSubmitButton()
            }
        })
    }

    /**
     * Enables AddAppointment-Button only if the input is valid.
     */
    private fun checkSubmitButton(){
        if(binding.etNote.error.isNullOrBlank() && binding.etTitle.error.isNullOrBlank()) {
            binding.btnAddAppointment.isEnabled = true
        }
    }
}
