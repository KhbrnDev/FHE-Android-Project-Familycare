package de.fhe.familycare.view.familymember.familyMemberViewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter for the FamilyMember ViewPager
 */
class FamilyMemberViewPagerAdapter(familyMemberId: Long, fragmentManager: FragmentManager, lifecycle: Lifecycle):
    FragmentStateAdapter(fragmentManager, lifecycle) {

    /**
     * creates tabs for Profile, WeightData and Appointments
     */
    private val fragmentArray:Array<Fragment> = arrayOf(
        FamilyMemberFragment.newInstance(familyMemberId),
        WeightDataFragment.newInstance(familyMemberId),
        FamilyMemberAppointmentFragment.newInstance(familyMemberId)
    )

    /**
     * @return size of the fragmentArray
     */
    override fun getItemCount(): Int {
        return fragmentArray.size
    }

    /**
     * creates fragment at given position
     * @param position the position of the fragment
     * @return the created fragment
     */
    override fun createFragment(position: Int): Fragment {
        return fragmentArray[position]
    }
}