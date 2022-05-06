package de.fhe.familycare.view.familymember.addFamilyMember

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentAddFamilyMemberBinding
import de.fhe.familycare.storage.core.FileSystemHandler
import de.fhe.familycare.storage.enums.Gender
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.storage.model.FamilyMemberType
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.TextValidator
import de.fhe.familycare.view.familymember.FamilyMemberViewModel
import java.io.File
import java.io.IOException



/**
 * Fragment of AddFamilyMember View
 */
class AddFamilyMemberFragment : BaseFragment() {

    private lateinit var navController: NavController

    private var _binding: FragmentAddFamilyMemberBinding? = null
    private val binding get() = _binding!!

    private lateinit var addFamilyMemberViewModel: FamilyMemberViewModel

    private var photoFile : File? = null
    private val REQUEST_IMAGE_GALERY = 420
    private val REQUEST_IMAGE_CAPTURE = 69 // nice
    private var currentPhotoPath: String? = null

    private lateinit var addTypeString: String

    private var height = 1

    /**
     * initializes navController and ViewModel
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFamilyMemberBinding.inflate(inflater, container, false)
        addFamilyMemberViewModel = this.getViewModel(FamilyMemberViewModel::class.java)
        addTypeString = getString(R.string.add_type)
        navController = findNavController()
        return binding.root
    }

    /**
     * performs input validation
     * populates dropdown
     * sets onClickListener to ProfileImage, AddHeight button, AddFamilyMember button
     * sets OnCheckChangedListener to IsHuman switch
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateTypeDropdown()
        inputValidation()

        binding.ivProfileImage.setOnClickListener {
            getNewProfileImageDialog()
        }

        binding.switchIsHuman.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.buttonAddHeight.visibility = View.VISIBLE
            }
            else {
                binding.buttonAddHeight.visibility = View.GONE
            }
        }

        binding.buttonAddHeight.setOnClickListener {
            heightInputDialog()
        }

        binding.btnAddFamilyMember.setOnClickListener {

            val familyMember = FamilyMember()
            familyMember.name = binding.etName.editText?.text.toString()
            familyMember.isCastrated = binding.switchIsCastrated.isChecked
            familyMember.isHuman = binding.switchIsHuman.isChecked

            if(height > 0){
                familyMember.height = height
            } else {
                familyMember.height = 1
            }

            familyMember.picturePath = this.currentPhotoPath ?: ""

            familyMember.birthdate = familyMember.formatBirthdate(binding.etBirthday.year, binding.etBirthday.month, binding.etBirthday.dayOfMonth)

            val checkedRadioButtonId = binding.radioGroupGender.checkedRadioButtonId
            familyMember.gender = when(checkedRadioButtonId){
                binding.radioButtonFemale.id -> Gender.FEMALE
                binding.radioButtonMale.id -> Gender.MALE
                binding.radioButtonNonBinary.id -> Gender.NONBINARY
                else -> Gender.NONBINARY
            }

            familyMember.note = binding.etNote.editText?.text.toString()

            val selectedType = binding.actvType.text.toString()
            if(selectedType == addTypeString){
                val newType = binding.etNewType.editText?.text.toString()
                addFamilyMemberViewModel.saveNewFamilyMemberType(newType)
                familyMember.type = FamilyMemberType(newType)
            }
            else{
                familyMember.type = FamilyMemberType(selectedType)
            }

            addFamilyMemberViewModel.saveFamilyMember(familyMember)

            super.hideKeyboard(this.requireContext(), this.requireView())

            navController.navigateUp()
        }
    }

    /**
     * Function to populate the Dropdown for type selection with all FamilyMemberTypes.
     * Checks also if a valid type is selected.
     */
    private fun populateTypeDropdown() {

        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList<String>())
        binding.actvType.setAdapter(typeAdapter)

        // set FamilyMemberType to addNewType initially
        typeAdapter.add(addTypeString)
        binding.actvType.setText(addTypeString, false)

        // get and set data
        addFamilyMemberViewModel.allFamilyMemberTypes.observe(this.viewLifecycleOwner) {familyMemberTypes ->
            Log.i("FM","typeSize = ${familyMemberTypes.size}")

            for(type in familyMemberTypes){
                Log.i("FM", "type = ${type.name}")

                // add all already available FamilyMemberTypes and push "addNewType" to end of list
                typeAdapter.remove(addTypeString)
                typeAdapter.add(type.name)
                typeAdapter.add(addTypeString)
            }
            typeAdapter.notifyDataSetChanged()
        }

        binding.actvType.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, position, _ ->
            Log.i("FM", "test + ${adapterView.getItemAtPosition(position)}")

            if(adapterView.getItemAtPosition(position).toString() == addTypeString){
                binding.etNewType.visibility = View.VISIBLE
                binding.etNewType.error = getString(R.string.type_not_blank)
                binding.btnAddFamilyMember.isEnabled = false
            } else {
                binding.etNewType.visibility = View.GONE

                // input validation reset on etNewType
                binding.etNewType.editText?.setText("")
                binding.etNewType.error = ""
            }
            checkSubmitButton()
        }
    }

    /**
     * Function to get profile picture from gallery
     */
    private fun profileImageGalleryListener(){

        Log.i("addFM", "method start")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        // Ensure that there's a gallery activity to handle the intent
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            // Create the File where the image should go
            photoFile = null
            Log.i("addFM", "Found a gallery app")
            try {
                photoFile = FileSystemHandler.createImageFile(requireContext())
                if (photoFile != null) {
                    currentPhotoPath = "file://" + photoFile!!.absolutePath
                }
            } catch (ex: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Could not create file for image",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                startActivityForResult(intent, REQUEST_IMAGE_GALERY)
            }
        }

        Log.i("addFM", "gallery method exited")
    }

    /**
     * Function to take picture with camera as profile picture
     */
    private fun profileImagePreviewClickListener() {

        Log.i("addFM", "method start")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            Log.i("addFM", "Found a camera app")
            try {
                photoFile = FileSystemHandler.createImageFile(requireContext())
                if (photoFile != null) {
                    currentPhotoPath = "file://" + photoFile.absolutePath
                }
            } catch (ex: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Could not create file for image",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "de.fhe.familycare.file_provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        Log.i("addFM", "camera method exited")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i("FM", "called: on Activity Result")
        // camera image capture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            this.binding.ivProfileImage.setImageURI(Uri.parse(currentPhotoPath))
        }

        // gallery image capture
        if(requestCode == REQUEST_IMAGE_GALERY && resultCode == RESULT_OK && data != null){
            try {
                if(data.data != null){
                    // get image Bitmap
                    val imageUri = data.data!!
                    val imageStream = requireContext().contentResolver.openInputStream(imageUri)
                    val imageBmp = BitmapFactory.decodeStream(imageStream)

                    FileSystemHandler.saveFile(photoFile!!, imageBmp)
                    binding.ivProfileImage.setImageURI(Uri.parse(currentPhotoPath))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), getText(R.string.general_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Function to open height input dialog and display selected height in view
     */
    private fun heightInputDialog(){

        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setTitle(getString(R.string.title_height_input))
        builder.setView(editText)

        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->

            binding.tvHeight.visibility = View.VISIBLE

            val etHeight = editText.text.toString()
            if(!etHeight.isNullOrBlank()){
                height = etHeight.toInt()
            }
            if(height < 1){
                height = 1
            }
            binding.tvHeight.text = getString(R.string.current_height, height.toString())

            dialogInterface.dismiss()
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
            dialogInterface.cancel()
        }

        builder.show()
    }

    /**
     * Checks all user input fields for valid input
     */
    private fun inputValidation(){

        // initial setup
        binding.etName.error = getString(R.string.name_not_blank)
        binding.etNewType.error = getString(R.string.new_type_not_blank)
        binding.btnAddFamilyMember.isEnabled = false

        // name validation
        val etName = binding.etName.editText
        etName?.addTextChangedListener(object : TextValidator(etName) {
            override fun validate(textView: TextView, text: String) {
                if(text.isBlank()){
                    binding.etName.error = getString(R.string.name_not_blank)
                    Log.i("FM", "error detected")
                    binding.btnAddFamilyMember.isEnabled = false
                    return
                }

                binding.etName.error = ""
                checkSubmitButton()
            }
        })

        // new Type validation
        val etNewType = binding.etNewType.editText
        etNewType?.addTextChangedListener(object : TextValidator(etNewType) {
            override fun validate(textView: TextView, text: String) {
                if(text.isBlank()){
                    binding.etNewType.error = getString(R.string.new_type_not_blank)
                    Log.i("FM", "error detected")
                    binding.btnAddFamilyMember.isEnabled = false
                    return
                }

                if(text.length > 50){
                    binding.etNewType.error = getString(R.string.new_type_too_long)
                    Log.i("FM", "New Type too long")
                    binding.btnAddFamilyMember.isEnabled = false
                    return
                }

                binding.etNewType.error = ""
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
                    binding.btnAddFamilyMember.isEnabled = false
                    return
                }
                binding.etNote.error = ""
                checkSubmitButton()
            }
        })

    }

    /**
     * Function that enables submit button if all input is valid.
     */
    private fun checkSubmitButton(){
        if(binding.etName.error.isNullOrBlank() && binding.etNewType.error.isNullOrBlank() && binding.etNote.error.isNullOrBlank()) {
            binding.btnAddFamilyMember.isEnabled = true
        }
    }

    /**
     * handle profile image input dialog
     */
    private fun getNewProfileImageDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_profile_picture_input))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setNegativeButton(getString(R.string.button_gallery)) { dialog, _ ->
                profileImageGalleryListener()
                dialog.dismiss()
            }
            .setPositiveButton(R.string.button_camera) { dialog, _ ->
                profileImagePreviewClickListener()
                dialog.dismiss()
            }
            .show()
    }
}