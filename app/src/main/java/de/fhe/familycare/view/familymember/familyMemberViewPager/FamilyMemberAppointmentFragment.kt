package de.fhe.familycare.view.familymember.familyMemberViewPager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.databinding.FragmentAllAppointmentsBinding
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.familymember.FamilyMemberViewModel

/**
 * Fragment for allAppointments of specific FamilyMember view
 */
class FamilyMemberAppointmentFragment : BaseFragment() {

    private val FAMILY_MEMBER_ID = "familyMemberId"

    private var _binding: FragmentAllAppointmentsBinding? = null

    private val binding get() = _binding!!

    private lateinit var familyMemberViewModel: FamilyMemberViewModel

    private var familyMemberId: Long? = null

    /**
     * sets familyMemberId
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            familyMemberId = it.getLong(FAMILY_MEMBER_ID)
        }
    }

    /**
     * initializes ViewModel
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Bind and Inflate the layout for this fragment
        _binding = FragmentAllAppointmentsBinding.inflate(inflater, container, false)
        familyMemberViewModel = this.getViewModel(FamilyMemberViewModel::class.java)

        return binding.root
    }

    /**
     * initializes navController
     * adds OnClickListener to AddAppointment-Button
     * Observes whether to load all Appointments or only future Appointments and
     * fills Adapter with the correct data
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.btnAddAppointment.setOnClickListener {
            val action = FamilyMemberViewPagerFragmentDirections
                .actionFamilyMemberAppointmentsToAddAppointmentFragment(familyMemberId!!)
            navController.navigate(action)
        }

        val adapter = FamilyMemberAppointmentAdapter {
            val action = FamilyMemberViewPagerFragmentDirections
                .actionFamilyMemberAppointmentsToShowAppointment(it.id )
            navController.navigate(action)
        }

        // initial recyclerview setup
        val rvAllAppointments = binding.rvAllAppointments
        rvAllAppointments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAllAppointments.adapter = adapter

        // initial data setup
        familyMemberViewModel.getAllFutureAppointmentsOfFamilyMemberById(familyMemberId!!)
            .observe(this.viewLifecycleOwner) { appointmentList ->
                adapter.submitList(appointmentList)
            }

        // switching data in recyclerview
        binding.switchShowPastAppointments.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                familyMemberViewModel.getAllAppointmentsOfFamilyMemberById(familyMemberId!!)
                    .observe(this.viewLifecycleOwner) { appointmentList ->
                        adapter.submitList(appointmentList)
                    }
            } else {
                familyMemberViewModel.getAllFutureAppointmentsOfFamilyMemberById(familyMemberId!!)
                    .observe(this.viewLifecycleOwner) { appointmentList ->
                        adapter.submitList(appointmentList)
                    }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(familyMemberId: Long) =
            FamilyMemberAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putLong(FAMILY_MEMBER_ID, familyMemberId)
                }
            }
    }
}