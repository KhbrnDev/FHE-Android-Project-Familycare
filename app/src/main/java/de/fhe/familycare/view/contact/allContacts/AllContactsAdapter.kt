package de.fhe.familycare.view.contact.allContacts

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fhe.familycare.R
import de.fhe.familycare.databinding.RvitemAllContactsBinding
import de.fhe.familycare.storage.model.Contact
import de.fhe.familycare.view.core.Util

/**
 * Adapter for AllContacts RecyclerView
 */
class AllContactsAdapter(private val onContactClicked: (Contact) -> Unit) :
    ListAdapter<Contact, AllContactsAdapter.AllContactsViewHolder>(DiffCallback) {

    class AllContactsViewHolder(var view: View) : RecyclerView.ViewHolder(view){

        private val binding = RvitemAllContactsBinding.bind(view)

        /**
         * Binds Contact Object to View and displays its data
         */
        fun bind(contact: Contact){

            binding.apply {
                tvName.text = contact.name

                // don't show and deactivate button
                if (contact.email.isNullOrBlank()) {
                    btnEmail.apply {
                        visibility = View.GONE
                        isEnabled = false
                    }
                }
                // don't show and deactivate button
                if (contact.phone.isNullOrBlank()) {
                    btnCall.apply {
                        isEnabled = false
                        visibility = View.GONE
                    }
                }
                // Keep spacing even if both buttons are disabled and Invisible
                if (contact.phone.isNullOrBlank() and contact.email.isNullOrBlank()) {
                    btnCall.visibility = View.INVISIBLE
                }

                // make call intent
                btnCall.setOnClickListener {
                    val dialNumberIntent = Intent(Intent.ACTION_DIAL)
                    dialNumberIntent.data = Uri.parse("tel:${contact.phone}")

                    Util.makeSingleImplicitIntent(dialNumberIntent, view)
                }

                // make email intent
                btnEmail.setOnClickListener {

                    val writeEmailIntent = Intent(Intent.ACTION_SENDTO)
                    writeEmailIntent.data = Uri.parse("mailto:${contact.email}")
                    writeEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "SubjectToIntent")

                    Util.makeSingleImplicitIntent(writeEmailIntent, view)
                }
            }
        }
    }

    /**
     * inflates Layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllContactsViewHolder {

        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.rvitem_all_contacts, parent, false)

        return AllContactsViewHolder(layout)
    }

    /**
     * Sets onClickListener to ListItems
     */
    override fun onBindViewHolder(holder: AllContactsViewHolder, position: Int) {

        val current = getItem(position)

        holder.itemView.setOnClickListener{
            onContactClicked(current)
        }

        holder.bind(current)
    }

    /**
     * Checks if item is already in List
     */
    companion object{
        private val DiffCallback = object : DiffUtil.ItemCallback<Contact>(){
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}