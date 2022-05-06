package de.fhe.familycare.view.contact.editContact

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentEditContactBinding
import de.fhe.familycare.storage.model.Contact
import de.fhe.familycare.view.contact.ContactViewModel
import de.fhe.familycare.view.contact.contact.ContactFragmentArgs
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.TextValidator

/**
 * Fragment for EditContact View
 */
class EditContactFragment : BaseFragment() {

    private val navigationArgs: ContactFragmentArgs by navArgs()

    private lateinit var navController: NavController
    private lateinit var contact: Contact

    private var _binding: FragmentEditContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactViewModel: ContactViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditContactBinding.inflate(inflater, container, false)
        contactViewModel = this.getViewModel(ContactViewModel::class.java)
        navController = findNavController()
        setHasOptionsMenu(true)
        return binding.root
    }

    /**
     * binds Contact to View
     * calls inputValidation
     * sets onClickListener to AddContactButton
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.contactID

        contactViewModel.getContact(id).observe(this.viewLifecycleOwner){
            contact = it
            bind(it)
        }


        inputValidation()

        binding.btnAddContact.setOnClickListener {

            val contact = Contact()
            contact.id = navigationArgs.contactID
            contact.name = binding.etName.editText?.text.toString()
            contact.phone = binding.etPhone.editText?.text.toString()
            contact.email = binding.etMail.editText?.text.toString()
            contact.note = binding.etNote.editText?.text.toString()

            contactViewModel.saveContact(contact)

            super.hideKeyboard(this.requireContext(), this.requireView())

            // go back to where you came from
            navController.navigateUp()
        }
    }


    /**
     * sets delete icon on appbar
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.delete_button_menu, menu)
    }

    /**
     * handles contact deletion selection and dialog
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.miDelete){
            Log.i("FM", "Delete Button selectted")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_contact))
                .setMessage(getString(R.string.contact_deletion_confirmation))
                .setPositiveButton(getString(R.string.delete)){dialog, _ ->
                    contactViewModel.deleteContact(contact.id)
                    // go back to the recyclerview
                    navController.navigateUp()
                    navController.navigateUp()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)){dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        return super.onOptionsItemSelected(item)

    }


    /**
     * Binds Contact to View
     */
    private fun bind(contact: Contact) {

        binding.apply {
            etName.editText?.setText(contact.name)
            etPhone.editText?.setText(contact.phone)
            etMail.editText?.setText(contact.email)
            etNote.editText?.setText(contact.note)
        }
    }

    /**
     * Check if submit button can be enabled if it was disabled
     * by checking the status of all validated input fields
     */
    private fun checkSubmitButton() {
        if (binding.etMail.error.isNullOrBlank() &&  binding.etPhone.error.isNullOrBlank() && binding.etNote.error.isNullOrBlank() && binding.etName.error.isNullOrBlank()) {
            binding.btnAddContact.isEnabled = true
        }
    }

    /**
     * Checks all user input fields for valid input
     */
    private fun inputValidation(){

        // initial setup
        binding.etName.error = getString(R.string.name_not_blank)

        // validate email input
        val etEmail = binding.etMail.editText
        etEmail?.addTextChangedListener(object : TextValidator(etEmail) {

            override fun validate(textView: TextView, text: String) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    if (text != "") {
                        binding.etMail.error = getString(R.string.no_phone_number)
                        Log.i("FM", "error detected")
                        binding.btnAddContact.isEnabled = false
                        return
                    }
                }

                binding.etMail.error = ""
                Log.i("FM", "success")
                checkSubmitButton()
            }
        })

        // validate phone number input
        val etPhone = binding.etPhone.editText
        etPhone?.addTextChangedListener(object : TextValidator(etPhone) {

            override fun validate(textView: TextView, text: String) {
                if (!Patterns.PHONE.matcher(text).matches()) {
                    if (text != "") {
                        binding.etPhone.error = getString(R.string.no_phone_number)
                        Log.i("FM", "No valid phone number")
                        binding.btnAddContact.isEnabled = false
                        return
                    }
                }
                if (text.length > 15) {
                    binding.etPhone.error = getString(R.string.phone_number_too_long)
                    Log.i("FM", "Phone number too long")
                    binding.btnAddContact.isEnabled = false
                    return
                }

                binding.etPhone.error = ""
                Log.i("FM", "success")
                checkSubmitButton()

            }
        })

        // validate name input
        val etName = binding.etName.editText
        etName?.addTextChangedListener(object : TextValidator(etName) {

            override fun validate(textView: TextView, text: String) {
                if (text.isBlank()) {
                    binding.etName.error = getString(R.string.name_not_blank)
                    Log.i("FM", "Name must not be blank")
                    binding.btnAddContact.isEnabled = false
                    return
                }
                binding.etName.error = ""
                Log.i("FM", "success")
                checkSubmitButton()
            }
        })

        // validate Note not longer than 255 characters
        val etNote = binding.etNote.editText
        etNote?.addTextChangedListener(object : TextValidator(etNote) {
            override fun validate(textView: TextView, text: String) {
                if (text.length > 255) {
                    binding.etNote.error = getString(R.string.note_too_long)
                    Log.i("FM", "error detected")
                    binding.btnAddContact.isEnabled = false
                    return
                }
                binding.etNote.error = ""
                checkSubmitButton()
            }
        })
    }
}
