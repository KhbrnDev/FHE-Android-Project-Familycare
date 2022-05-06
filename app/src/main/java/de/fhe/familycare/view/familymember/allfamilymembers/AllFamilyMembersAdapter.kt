package de.fhe.familycare.view.familymember.allfamilymembers

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.R
import de.fhe.familycare.databinding.RvitemAllFamilymembersBinding
import de.fhe.familycare.storage.model.FamilyMember

/**
 * Adapter for AllFamilyMembers RecyclerView
 */
class AllFamilyMembersAdapter(private val onFamilyMemberClicked: (FamilyMember) -> Unit) :
    ListAdapter<FamilyMember, AllFamilyMembersAdapter.AllFamilyMembersViewHolder>(DiffCallback) {

    class AllFamilyMembersViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val binding = RvitemAllFamilymembersBinding.bind(view)

        /**
         * Binds FamilyMember Object to View and displays its data
         */
        fun bind(familyMember: FamilyMember){

            binding.tvName.text = familyMember.name
            binding.tvBirthday.text = familyMember.birthdate
            if(familyMember.picturePath.isNullOrBlank()){
                binding.ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
            }else{
                binding.ivProfileImage.setImageURI(Uri.parse(familyMember.picturePath))
            }
            if (familyMember.type != null) {
                binding.tvType.text = familyMember.type!!.name
                }
        }
    }

    /**
     * inflates Layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllFamilyMembersViewHolder {

        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.rvitem_all_familymembers, parent, false)

        return AllFamilyMembersViewHolder(layout)
    }

    /**
     * Sets onClickListener to ListItems
     */
    override fun onBindViewHolder(holder: AllFamilyMembersViewHolder, position: Int) {

        val current = getItem(position)

        holder.itemView.setOnClickListener{
            onFamilyMemberClicked(current)
        }

        holder.bind(current)
    }

    /**
     * Checks if item is already in List
     */
    companion object{
        private val DiffCallback = object : DiffUtil.ItemCallback<FamilyMember>(){
            override fun areItemsTheSame(oldItem: FamilyMember, newItem: FamilyMember): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: FamilyMember, newItem: FamilyMember): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}