package de.fhe.familycare.view.appointments.editAppointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentEditAppointmentBinding
import de.fhe.familycare.storage.model.Appointment
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.view.appointments.AppointmentViewModel
import de.fhe.familycare.view.appointments.appointment.AppointmentFragmentArgs
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.TextValidator
import java.util.*

/**
 * Fragment of EditAppointment View
 */
class EditAppointmentFragment : BaseFragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private val navigationArgs: AppointmentFragmentArgs by navArgs()

    private lateinit var navController: NavController
    private lateinit var appointment: Appointment

    private var _binding: FragmentEditAppointmentBinding? = null
    private val binding get() = _binding!!

    private var familyMemberList = mutableListOf<FamilyMember>()

    private lateinit var appointmentViewModel: AppointmentViewModel

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
        _binding = FragmentEditAppointmentBinding.inflate(inflater, container, false)
        appointmentViewModel = this.getViewModel(AppointmentViewModel::class.java)
        navController = findNavController()
        setHasOptionsMenu(true)
        return binding.root
    }

    /**
     * populate Dropdown for FamilyMember selection
     * initiate input validation
     * sets OnClickListener to AddAppointment Button
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.appointmentID

        appointmentViewModel.getAppointment(id).observe(this.viewLifecycleOwner) {
            appointment = it
            bind(it)
        }

        populateSelectFamilyMemberDropdown()
        inputValidation()


        binding.buttonAddStartTime.setOnClickListener {

            getDateTimeCalendar()
            DatePickerDialog(requireContext(), this, year, month, day).show()
        }

        binding.btnAddAppointment.setOnClickListener {

            val appointment = Appointment()
            appointment.id = navigationArgs.appointmentID
            appointment.title = binding.etTitle.editText?.text.toString()

            val tmpStartDate = binding.tvStartTimeText.text.toString()

            appointment.date_start = Appointment.parseDate(tmpStartDate)

            appointment.familyMemberID =
                getIdOfFamilyMember(binding.actvFamilyMember.text.toString())

            appointment.familyMemberName = binding.actvFamilyMember.text.toString()

            appointment.note = binding.etNote.editText?.text.toString()

            appointmentViewModel.saveAppointment(appointment)

            super.hideKeyboard(this.requireContext(), this.requireView())

            // go to back to where you came from
            navController.navigateUp()
        }
    }

    /**
     * sets delete icon on appbar
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.delete_button_menu, menu)
    }

    /**
     * handles appointment deletion selection and dialog
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.miDelete){
            Log.i("FM", "Delete Button selectted")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_appointment))
                .setMessage(getString(R.string.appointment_deletion_confirmation))
                .setPositiveButton(getString(R.string.delete)){dialog, _ ->
                    appointmentViewModel.deleteAppointment(appointment.id)
                    // go back to the recyclerview
                    navController.navigateUp()
                    navController.navigateUp()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)){dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        return super.onOptionsItemSelected(item)

    }

    /**
     * creates an Calendar instance
     */
    private fun getDateTimeCalendar() {
        if(savedDay.isNullOrBlank() || savedMonth.isNullOrBlank() || savedYear.isNullOrBlank() || savedHour.isNullOrBlank() || savedMinute.isNullOrBlank()) {
            // bind date to datepicker
            year = appointment.date_start?.year!!
            month = appointment.date_start?.month?.value!! - 1
            day = appointment.date_start?.dayOfMonth!!
            hour = appointment.date_start?.hour!!
            minute = appointment.date_start?.minute!!
        } else {
            day = savedDay.toInt()
            month = savedMonth.toInt() -1
            year = savedYear.toInt()
            hour = savedHour.toInt()
            minute = savedMinute.toInt()
        }

    }


    /**
     * Binds Appointment to View
     * @param appointment: The given Appointment for the View
     */
    private fun bind(appointment: Appointment) {

        binding.apply {

            etTitle.editText?.setText(appointment.title)
            tvStartTimeText.text = appointment.formatStartDate()
            actvFamilyMember.setText(
                "${appointment.familyMemberName ?: getText(R.string.no_family_member)}",false
            )
            if (appointment.familyMemberName.isNullOrBlank()) {
                actvFamilyMember.setText(getText(R.string.no_family_member), false)
            }
            etNote.editText?.setText(appointment.note)
        }
    }

    /**
     * Function to load all existing FamilyMembers into the SelectFamilyMemberDropdown
     */
    private fun populateSelectFamilyMemberDropdown() {
        val familyMemberAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList<String>())
        binding.actvFamilyMember.setAdapter(familyMemberAdapter)
        familyMemberAdapter.add(getText(R.string.no_family_member).toString())

        // get and set data
        appointmentViewModel.allFamilyMembers.observe(this.viewLifecycleOwner) { familyMembers ->
            familyMemberList = familyMembers.toMutableList()
            Log.i("FM", "typeSize = ${familyMembers.size}")

            familyMembers.forEach {
                Log.i("FM", "member = ${it.name}")
                familyMemberAdapter.add(it.name)
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
