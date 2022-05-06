package de.fhe.familycare.view.appointments.appointment

import android.os.Bundle
import android.view.*
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentAppointmentBinding
import de.fhe.familycare.storage.model.Appointment
import de.fhe.familycare.view.appointments.AppointmentViewModel
import de.fhe.familycare.view.core.BaseFragment

/**
 * Fragment for detail view of single Appointment
 */
class AppointmentFragment : BaseFragment() {

    private val navigationArgs: AppointmentFragmentArgs by navArgs()

    private var _binding: FragmentAppointmentBinding? = null

    private val binding get() = _binding!!

    lateinit var navController: NavController

    private lateinit var appointmentViewModel: AppointmentViewModel

    /**
     * initializes ViewModel and bind and inflates layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Bind and Inflate the layout for this fragment
        _binding = FragmentAppointmentBinding.inflate(inflater, container, false)
        appointmentViewModel = this.getViewModel(AppointmentViewModel::class.java)
        navController = findNavController()
        setHasOptionsMenu(true)
        return binding.root
    }

    /**
     * gets appointment from viewmodel observer
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appointmentViewModel.getAppointment(navigationArgs.appointmentID).observe(this.viewLifecycleOwner) {
            bind(it)
        }
    }

    /**
     * sets edit icon in appbar
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_button_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * handles navigation to edit appointment view when appbar icon is clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(R.id.miEdit == item.itemId){
            val action = AppointmentFragmentDirections.actionShowAppointmentToEditAppointment(navigationArgs.appointmentID)
            navController.navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * binds Appointment to view
     * @param appointment: The Appointment that should be displayed in the view
     */
    private fun bind(appointment: Appointment) {

        binding.apply {
            tvTitle.text = appointment.title
            tvStartDate.text = getString(R.string.appointment_clock, appointment.formatStartDate())
            tvCorrespondingFamilyMember.text =
                getString(R.string.appointment_with_familymember,
                    appointment.familyMemberName ?: getText(R.string.no_family_member))

            if (appointment.note != "") {
                tvNote.text = getString(R.string.note_with_text, appointment.note)
            }
        }
    }
}