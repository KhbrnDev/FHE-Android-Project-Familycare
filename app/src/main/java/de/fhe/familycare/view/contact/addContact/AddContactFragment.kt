package de.fhe.familycare.view.contact.addContact

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentAddContactBinding
import de.fhe.familycare.storage.model.Contact
import de.fhe.familycare.view.contact.ContactViewModel
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.TextValidator

/**
 * Fragment of AddContact View
 */
class AddContactFragment : BaseFragment(){

    private lateinit var navController: NavController

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var addContactViewModel: ContactViewModel

    /**
     * initializes navController and addContactViewModel
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        addContactViewModel = this.getViewModel(ContactViewModel::class.java)
        navController = findNavController()
        return binding.root
    }

    /**
     * performs input validation
     * sets onClickListener to AddContact-Button
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputValidation()

        binding.btnAddContact.setOnClickListener {

            val contact = Contact()
            contact.name = binding.etName.editText?.text.toString()
            contact.phone = binding.etPhone.editText?.text.toString()
            contact.email = binding.etMail.editText?.text.toString()
            contact.note = binding.etNote.editText?.text.toString()

            addContactViewModel.saveContact(contact)

            super.hideKeyboard(this.requireContext(), this.requireView())

            // go back to where you came from
            navController.navigateUp()
        }
    }

    /**
     * Enables AddContact-Button only if the input is valid.
     */
    private fun checkSubmitButton(){
        if(binding.etMail.error.isNullOrBlank() && binding.etPhone.error.isNullOrBlank() && binding.etNote.error.isNullOrBlank() && binding.etName.error.isNullOrBlank()) {
            binding.btnAddContact.isEnabled = true
        }
    }

    /**
     * Checks all user input fields for valid input
     */
    private fun inputValidation(){

        // initial setup
        binding.etName.error = getString(R.string.name_not_blank)
        binding.btnAddContact.isEnabled = false

        // validate email input
        val etEmail = binding.etMail.editText
        etEmail?.addTextChangedListener(object : TextValidator(etEmail) {

            override fun validate(textView: TextView, text: String) {
                if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    if (text != "") {
                        binding.etMail.error = getString(R.string.no_email_address)
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
