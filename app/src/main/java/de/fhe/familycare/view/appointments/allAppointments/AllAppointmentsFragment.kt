package de.fhe.familycare.view.appointments.allAppointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.databinding.FragmentAllAppointmentsBinding
import de.fhe.familycare.view.appointments.AppointmentViewModel
import de.fhe.familycare.view.core.BaseFragment

/**
 * Fragment for allAppointments view
 */
class AllAppointmentsFragment : BaseFragment() {

    private var _binding: FragmentAllAppointmentsBinding? = null

    private val binding get() = _binding!!

    private lateinit var appointmentViewModel: AppointmentViewModel

    /**
     * initializes ViewModel
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Bind and Inflate the layout for this fragment
        _binding = FragmentAllAppointmentsBinding.inflate(inflater, container, false)
        appointmentViewModel = this.getViewModel(AppointmentViewModel::class.java)
        return binding.root
    }

    /**
     * initializes navController
     * adds OnClickListener to AddAppointment-Button
     * Observes whether to load all Appointments or only future Appointments and
     * fills AllAppointmentsAdapter with the correct data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.btnAddAppointment.setOnClickListener {
            val action =
                AllAppointmentsFragmentDirections.actionMiAllAppointmentsToAddAppointmentFragment(-1)
            navController.navigate(action)
        }

        val adapter = AllAppointmentsAdapter {
            val action =
                AllAppointmentsFragmentDirections.actionMiAllAppointmentsToShowAppointment(it.id)
            navController.navigate(action)
        }

        // initial recyclerview setup
        val rvAllAppointments = binding.rvAllAppointments
        rvAllAppointments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAllAppointments.adapter = adapter

        // initial data setup
        appointmentViewModel.allFutureAppointments.observe(this.viewLifecycleOwner) { appointmentList ->
            adapter.submitList(appointmentList)
        }

        // switching data in recyclerview
        binding.switchShowPastAppointments.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                appointmentViewModel.allAppointments.observe(this.viewLifecycleOwner) { appointmentList ->
                    adapter.submitList(appointmentList)
                }
            } else {
                appointmentViewModel.allFutureAppointments.observe(this.viewLifecycleOwner) { appointmentList ->
                    adapter.submitList(appointmentList)
                }
            }
        }
    }
}