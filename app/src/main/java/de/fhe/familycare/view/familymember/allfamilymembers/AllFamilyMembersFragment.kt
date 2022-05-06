package de.fhe.familycare.view.familymember.allfamilymembers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.databinding.FragmentAllFamilyMembersBinding
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.familymember.FamilyMemberViewModel

/**
 * Fragment for allFamilyMembers view
 */
class AllFamilyMembersFragment : BaseFragment() {

    private var _binding: FragmentAllFamilyMembersBinding? = null

    private  val binding get() = _binding!!

    /**
     * initializes ViewModel
     * @return binding
     */
    private lateinit var familyMemberViewModel: FamilyMemberViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Bind and Inflate the layout for this fragment
        _binding = FragmentAllFamilyMembersBinding.inflate(inflater, container, false)

        familyMemberViewModel = this.getViewModel(FamilyMemberViewModel::class.java)

        return binding.root
    }

    /**
     * initializes navController
     * adds OnClickListener to AddFamilyMember-Button
     * adds OnCheckedChangedListener to ShowAllSwitch and fills AllFamilyMembersAdapter according to it
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        binding.btnAddFamilyMember.setOnClickListener{
            val action = AllFamilyMembersFragmentDirections.actionMiAllFamilyMembersToAddFamilyMemberFragment()
            navController.navigate(action)
        }

        val adapter = AllFamilyMembersAdapter{
            val action = AllFamilyMembersFragmentDirections.actionMiAllFamilyMembersToFamilyMemberViewPagerFragment(it.id)
            navController.navigate(action)
        }

        val rvAllFamilyMembers = binding.rvAllFamilyMembers
        rvAllFamilyMembers.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAllFamilyMembers.adapter = adapter

        familyMemberViewModel.allActiveFamilyMembers.observe(this.viewLifecycleOwner){familyMembers ->
            familyMembers.let{
                adapter.submitList(it)
            }
        }

        binding.switchShowAll.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                familyMemberViewModel.allFamilyMembers.observe(this.viewLifecycleOwner) { familyMembers ->
                    familyMembers.let {
                        adapter.submitList(it)
                    }
                }
            }
            else {
                familyMemberViewModel.allActiveFamilyMembers.observe(this.viewLifecycleOwner){familyMembers ->
                    familyMembers.let{
                        adapter.submitList(it)
                    }
                }
            }
        }
    }



}