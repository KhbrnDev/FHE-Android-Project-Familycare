package de.fhe.familycare.view.familymember.familyMemberViewPager

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentFamilyMemberBinding
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.storage.model.WeightData
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.familymember.FamilyMemberViewModel

/**
 * Fragment for FamilyMember View
 */
class FamilyMemberFragment : BaseFragment() {

    private val FAMILY_MEMBER_ID = "familyMmeberId"

    private var _binding: FragmentFamilyMemberBinding? = null

    private  val binding get() = _binding!!

    private lateinit var familyMemberViewModel: FamilyMemberViewModel

    private lateinit var navController: NavController
    private lateinit var familyMember: FamilyMember
    private var lastWeightData: WeightData? = null

    private var familyMemberId: Long? = null

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
        _binding = FragmentFamilyMemberBinding.inflate(inflater, container, false)
        familyMemberViewModel = this.getViewModel(FamilyMemberViewModel::class.java)
        navController = findNavController()
        setHasOptionsMenu(true)
        return binding.root
    }


    /**
     * adds OnClickListeners to buttons
     * observes LiveData of FamilyMember and LastWeightData
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        familyMemberViewModel.getFamilyMember(familyMemberId!!).observe(this.viewLifecycleOwner){
            familyMember = it
            bind(it)
        }

        familyMemberViewModel.getLastWeightDataByFamilyMemberId(familyMemberId!!).observe(this.viewLifecycleOwner){
            lastWeightData = it
        }

    }

    /**
     * sets edit icon in appbar
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_button_menu, menu)
    }

    /**
     * handles navigation to edit familymember view when appbar icon is clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.miEdit){
            val action = FamilyMemberViewPagerFragmentDirections.actionShowFamilyMemberToEditFamilyMemberFragment(familyMemberId!!)
            navController.navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance(familyMemberId: Long) =
            FamilyMemberFragment().apply {
                arguments = Bundle().apply {
                    putLong(FAMILY_MEMBER_ID, familyMemberId)
                }
            }
    }

    /**
     * binds FamilyMember to View and displays data of FamilyMember
     * @param familyMember the FamilyMember to be displayed
     */
    private fun bind(familyMember: FamilyMember){

        binding.apply {

            tvName.text = familyMember.name
            tvGender.text = familyMember.gender.toString()
            tvBirthday.text = getString(R.string.birthday_with_text, familyMember.birthdate)
            tvFamilyMemberType.text = familyMember.type!!.name
            tvHeight.text = getString(R.string.height, familyMember.height.toString())
            tvNote.text = getString(R.string.note_with_text, familyMember.note)
            if(familyMember.isActive){
                tvIsActive.text = getString(R.string.is_active_familymember)
            }
            else{
                tvIsActive.text = getString(R.string.is_not_active)
            }

            if(familyMember.isHuman){
                tvIsHuman.text = getString(R.string.is_a_human)
            }
            else{
                tvIsHuman.text = getString(R.string.is_a_pet)
            }

            if(familyMember.isCastrated){
                tvIsCastrated.text = getString(R.string.is_castrated)
            }
            else{
                tvIsCastrated.text = getString(R.string.is_not_castrated)
            }


            if (familyMember.picturePath.isNullOrBlank()){
                ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
            }else{
                ivProfileImage.setImageURI(Uri.parse(familyMember.picturePath))
            }
        }
    }

}