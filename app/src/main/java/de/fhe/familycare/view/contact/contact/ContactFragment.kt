package de.fhe.familycare.view.contact.contact

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentContactBinding
import de.fhe.familycare.storage.model.Contact
import de.fhe.familycare.view.contact.ContactViewModel
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.Util

/**
 * Fragment for detail view of single Contact
 */
class ContactFragment : BaseFragment() {

    private val navigationArgs: ContactFragmentArgs by navArgs()

    private var _binding: FragmentContactBinding? = null

    private  val binding get() = _binding!!

    private lateinit var contactViewModel: ContactViewModel

    private lateinit var navController : NavController
    private lateinit var contact: Contact

    /**
     * initializes ViewModel and bind and inflates layout
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Bind and Inflate the layout for this fragment
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        contactViewModel = this.getViewModel(ContactViewModel::class.java)
        navController = findNavController()
        setHasOptionsMenu(true)
        return binding.root
    }

    /**
     * sets OnClickListeners to EditContact Button, Call Button and EMail Button
     * Binds Contact to view
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.contactID


        binding.btnCall.setOnClickListener {
            val dialNumberIntent = Intent(Intent.ACTION_DIAL)
            dialNumberIntent.data = Uri.parse("tel:${contact.phone}")

            Util.makeSingleImplicitIntent(dialNumberIntent, view)
        }

        binding.btnEmail.setOnClickListener {

            val writeEmailIntent = Intent(Intent.ACTION_SENDTO)
            writeEmailIntent.data = Uri.parse("mailto:${contact.email}")
            writeEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "SubjectToIntent")

            Util.makeSingleImplicitIntent(writeEmailIntent, view)
        }


        contactViewModel.getContact(id).observe(this.viewLifecycleOwner){
            contact = it
            bind(it)
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
     * handles navigation to edit contact view when appbar icon is clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.miEdit){
            val action = ContactFragmentDirections.actionShowContactToEditContact(navigationArgs.contactID)
            navController.navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * binds Contact to view
     * @param contact: The contact that should be displayed in the view
     */
    private fun bind(contact: Contact){

        binding.apply {
            tvName.text = contact.name
            tvMail.text = getString(R.string.email_with_text, contact.email)
            tvPhone.text = getString(R.string.phone_with_text, contact.phone)
            if(!contact.note.isNullOrBlank()){
                tvNote.text = getString(R.string.note_with_text, contact.note)
            }
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
                btnEmail.visibility = View.INVISIBLE
            }
        }
    }
}