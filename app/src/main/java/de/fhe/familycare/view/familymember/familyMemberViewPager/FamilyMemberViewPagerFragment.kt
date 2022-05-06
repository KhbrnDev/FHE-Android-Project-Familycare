package de.fhe.familycare.view.familymember.familyMemberViewPager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentFamilyMemberViewPagerBinding
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.MainActivity
import de.fhe.familycare.view.familymember.FamilyMemberViewModel

/**
 * Fragment for the FamilyMember ViewPager
 */
class FamilyMemberViewPagerFragment : BaseFragment(){

    private lateinit var rootView: View

    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: FamilyMemberViewPagerAdapter

    private val navigationArgs: FamilyMemberViewPagerFragmentArgs by navArgs()

    private var _binding: FragmentFamilyMemberViewPagerBinding? = null

    private  val binding get() = _binding!!

    private lateinit var familyMemberViewModel: FamilyMemberViewModel

    /**
     * initializes ViewModel and observes FamilyMember LiveData
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyMemberViewPagerBinding.inflate(inflater, container, false)
        rootView = inflater.inflate(
            R.layout.fragment_family_member_view_pager,container,false)

        familyMemberViewModel = this.getViewModel(FamilyMemberViewModel::class.java)

        // set actionbar title to familymember name
        familyMemberViewModel.getFamilyMember(navigationArgs.familyMemberID).observe(this.viewLifecycleOwner){
            (activity as MainActivity?)?.setActionBarTitle(it.name)
        }

        initViewPager2()
        return binding.root
    }


    /**
     * Initializes ViewPager, sets names of Tabs and sets adapter
     */
    private fun initViewPager2()
    {
        viewPager2 = binding.pagerViewpager
        adapter = FamilyMemberViewPagerAdapter(navigationArgs.familyMemberID, childFragmentManager, lifecycle)
        viewPager2.adapter = adapter

        tabLayout = binding.tabLayout
        val tabNames = arrayOf(getString(R.string.tab_profile_title), getString(R.string.tab_weight_title), getString(R.string.tab_appointment_title) )
        TabLayoutMediator(tabLayout,viewPager2){tab,position ->
            tab.text = tabNames[position]
        }.attach()
    }
}