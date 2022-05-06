package de.fhe.familycare.view.familymember.editFamilyMember

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
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
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentEditFamilyMemberBinding
import de.fhe.familycare.storage.core.FileSystemHandler
import de.fhe.familycare.storage.enums.Gender
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.storage.model.FamilyMemberType
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.core.TextValidator
import de.fhe.familycare.view.familymember.FamilyMemberViewModel
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Fragment of EditFamilyMember View
 */
class EditFamilyMemberFragment : BaseFragment(), DatePickerDialog.OnDateSetListener {

    private val navigationArgs: EditFamilyMemberFragmentArgs by navArgs()

    private lateinit var familyMember: FamilyMember

    private lateinit var navController: NavController

    private var _binding: FragmentEditFamilyMemberBinding? = null
    private val binding get() = _binding!!

    private lateinit var editFamilyMemberViewModel: FamilyMemberViewModel

    // variables for birthdate input
    private var day = 0
    private var month = -1
    private var year = 0

    private var savedDay = ""
    private var savedMonth = ""
    private var savedYear = ""

    private var height = 1

    private var photoFile: File? = null
    private val REQUEST_IMAGE_GALERY = 420
    private val REQUEST_IMAGE_CAPTURE = 69 // nice

    /**
     * this is only not null if a new picture was taken
     */
    private var currentPhotoPath: String? = null

    private lateinit var addTypeString: String

    /**
     * initialize ViewModel and NavController
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditFamilyMemberBinding.inflate(inflater, container, false)
        editFamilyMemberViewModel = this.getViewModel(FamilyMemberViewModel::class.java)
        navController = findNavController()
        addTypeString = getString(R.string.create_new_type)

        return binding.root
    }

    /**
     * populates dropdown for FamilyMemberType Selection
     * binds FamilyMember to view
     * initiates input validation
     * sets listeners to buttons and switches
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.familyMemberID

        editFamilyMemberViewModel.getFamilyMember(id).observe(this.viewLifecycleOwner) {
            familyMember = it
            bind(it)
        }

        populateTypeDropdown()
        inputValidation()

        binding.ivProfileImage.setOnClickListener {
            editProfileImageDialog()
        }

        binding.switchIsHuman.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.buttonAddHeight.visibility = View.VISIBLE
            } else {
                binding.buttonAddHeight.visibility = View.GONE
            }
        }

        binding.buttonAddHeight.setOnClickListener {
            heightInputDialog()
        }

        binding.buttonEditBirthday.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(requireContext(), this, year, month, day).show()
        }

        binding.btnEditFamilyMember.setOnClickListener {

            val newFamilyMember = FamilyMember()
            newFamilyMember.id = id
            newFamilyMember.name = binding.etName.editText?.text.toString()
            newFamilyMember.isCastrated = binding.switchIsCastrated.isChecked
            newFamilyMember.isHuman = binding.switchIsHuman.isChecked
            newFamilyMember.isActive = binding.switchIsArchived.isChecked

            if (height != 0) {
                newFamilyMember.height = height
            }

            newFamilyMember.picturePath = familyMember.picturePath

            newFamilyMember.birthdate = binding.tvBirthday.text.toString()

            val checkedRadioButtonId = binding.radioGroupGender.checkedRadioButtonId
            newFamilyMember.gender = when (checkedRadioButtonId) {
                binding.radioButtonFemale.id -> Gender.FEMALE
                binding.radioButtonMale.id -> Gender.MALE
                binding.radioButtonNonBinary.id -> Gender.NONBINARY
                else -> Gender.NONBINARY
            }

            newFamilyMember.note = binding.etNote.editText?.text.toString()

            val selectedType = binding.actvType.text.toString()
            if (selectedType == addTypeString) {
                val newType = binding.etNewType.editText?.text.toString()
                editFamilyMemberViewModel.saveNewFamilyMemberType(newType)
                newFamilyMember.type = FamilyMemberType(newType)
            } else {
                newFamilyMember.type = FamilyMemberType(selectedType)
            }

            editFamilyMemberViewModel.saveFamilyMember(newFamilyMember)

            super.hideKeyboard(this.requireContext(), this.requireView())

            navController.navigateUp()
        }
    }

    /**
     * Populates AddFamilyMemberType Dropdown with all existing FamilyMemberTypes
     */
    private fun populateTypeDropdown() {
        val typeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList<String>())
        binding.actvType.setAdapter(typeAdapter)

        typeAdapter.add(addTypeString)

        // get and set data
        editFamilyMemberViewModel.allFamilyMemberTypes.observe(this.viewLifecycleOwner) { familyMemberTypes ->
            Log.i("FM", "typeSize = ${familyMemberTypes.size}")

            for (type in familyMemberTypes) {
                Log.i("FM", "type = ${type.name}")

                // add all already available FamilyMemberTypes and push "addNewType" to end of list
                typeAdapter.remove(addTypeString)
                typeAdapter.add(type.name)
                typeAdapter.add(addTypeString)
            }
            typeAdapter.notifyDataSetChanged()
        }

        binding.actvType.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, position, _ ->
                Log.i("FM", "test + ${adapterView.getItemAtPosition(position)}")

                if (adapterView.getItemAtPosition(position).toString() == addTypeString) {
                    binding.etNewType.visibility = View.VISIBLE
                    binding.etNewType.error = getString(R.string.new_type_not_blank)
                    binding.btnEditFamilyMember.isEnabled = false
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
     * make intent to get and save image by capturing an image with camera
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
                    getText(R.string.file_not_created),
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


    /**
     * make intent to get and save an image from gallery
     */
    private fun profileImageGalleryListener() {


        Log.i("addFM", "method start")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            // Create the File where the photo should go
            photoFile = null
            Log.i("addFM", "Found a galary app")
            try {
                photoFile = FileSystemHandler.createImageFile(requireContext())
                if (photoFile != null) {
                    currentPhotoPath = "file://" + photoFile!!.absolutePath
                }
            } catch (ex: IOException) {
                Toast.makeText(
                    requireContext(),
                    getText(R.string.file_not_created),
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                startActivityForResult(intent, REQUEST_IMAGE_GALERY)
            }
        }
        Log.i("addFM", "camera method exited")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i("FM", "called: on Activity Result")
        // camera image capture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            familyMember.picturePath = currentPhotoPath
            editFamilyMemberViewModel.saveFamilyMember(familyMember)
        }

        // gallery image capture
        if (requestCode == REQUEST_IMAGE_GALERY && resultCode == RESULT_OK && data != null) {
            try {
                if (data.data != null) {
                    // get image Bitmap
                    val imageUri = data.data!!
                    val imageStream = requireContext().contentResolver.openInputStream(imageUri)
                    val imageBmp = BitmapFactory.decodeStream(imageStream)

                    FileSystemHandler.saveFile(photoFile!!, imageBmp)
                    familyMember.picturePath = currentPhotoPath
                    editFamilyMemberViewModel.saveFamilyMember(familyMember)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), getText(R.string.general_error), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /**
     * Binds FamilyMember to View
     * @param familyMember: The given FamilyMember for the View
     */
    private fun bind(familyMember: FamilyMember) {

        binding.apply {
            etName.editText?.setText(familyMember.name)
            switchIsCastrated.isChecked = familyMember.isCastrated
            switchIsHuman.isChecked = familyMember.isHuman
            switchIsArchived.isChecked = familyMember.isActive
            if (familyMember.height > 0) {
                tvHeight.visibility = View.VISIBLE
                tvHeight.text = getString(R.string.current_height, familyMember.height.toString())

            }
            if (!familyMember.picturePath.isNullOrBlank()) {
                ivProfileImage.setImageURI(Uri.parse(familyMember.picturePath))
            }
            tvBirthday.text = familyMember.birthdate

            when (familyMember.gender) {
                Gender.FEMALE -> {
                    radioGroupGender.check(radioButtonFemale.id)
                }
                Gender.MALE -> {
                    radioGroupGender.check(radioButtonMale.id)
                }
                Gender.NONBINARY -> {
                    radioGroupGender.check(radioButtonNonBinary.id)
                }
            }

            etNote.editText?.setText(familyMember.note)
            actvType.setText(familyMember.type?.name, false)
        }
    }


    /**
     * Instantiates Calendar object
     */
    private fun getDateTimeCalendar() {
        if(savedDay.isNullOrBlank() || savedMonth.isNullOrBlank() || savedYear.isNullOrBlank()){
            val germanFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val germanFormattedLocalTime =  LocalDate.parse(familyMember.birthdate, germanFormatter)!!
            val ldtFormatter = LocalDate.parse(germanFormattedLocalTime.toString())
            Log.i("FM", "year = ${ldtFormatter.year} | month = ${ldtFormatter.monthValue} | day = ${ldtFormatter.dayOfMonth}")
            day = ldtFormatter.dayOfMonth
            month = ldtFormatter.monthValue -1
            year = ldtFormatter.year
        } else {
            year = savedYear.toInt()
            month = savedMonth.toInt() - 1
            day = savedDay.toInt()
        }


    }

    /**
     * writes selected date to variables and displays it in Birthday TextView
     */
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = if (dayOfMonth > 9) {
            dayOfMonth.toString()
        } else {
            "0$dayOfMonth"
        }
        savedMonth = if ((month + 1) > 9) {
            (month + 1).toString()
        } else {
            "0" + (month + 1).toString()
        }

        savedYear = year.toString()

        binding.tvBirthday.text =
            getString(R.string.birthday_formatted, savedDay, savedMonth, savedYear)
    }

    /**
     * generates input dialog to add height of FamilyMember
     */
    private fun heightInputDialog() {

        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setTitle(getString(R.string.title_height_input))
        builder.setView(editText)

        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->

            binding.tvHeight.visibility = View.VISIBLE
            val etHeight = editText.text.toString()
            if (!etHeight.isNullOrBlank()) {
                height = etHeight.toInt()
            }

            if (height < 1) {
                height = 1
            }

            binding.tvHeight.text = getString(R.string.current_height, height.toString())


            dialogInterface.dismiss()
        }


        builder.setNegativeButton("Cancel") { dialogInterface, _ ->

            dialogInterface.cancel()
        }

        builder.show()
    }

    /**
     * Checks all user input fields for valid input
     */
    private fun inputValidation() {

        //validate input
        val etName = binding.etName.editText
        etName?.addTextChangedListener(object : TextValidator(etName) {
            override fun validate(textView: TextView, text: String) {
                if (text.isBlank()) {
                    binding.etName.error = getString(R.string.name_not_blank)
                    Log.i("FM", "error detected")
                    binding.btnEditFamilyMember.isEnabled = false
                    return
                }
                binding.etName.error = ""
                binding.btnEditFamilyMember.isEnabled = true
            }
        })

        // new Type validation
        val etNewType = binding.etNewType.editText
        etNewType?.addTextChangedListener(object : TextValidator(etNewType) {
            override fun validate(textView: TextView, text: String) {
                if (text.isBlank()) {
                    binding.etNewType.error = getString(R.string.new_type_not_blank)
                    Log.i("FM", "error detected")
                    binding.btnEditFamilyMember.isEnabled = false
                    return
                }

                if (text.length > 50) {
                    binding.etNewType.error = getString(R.string.new_type_too_long)
                    Log.i("FM", "New Type too long")
                    binding.btnEditFamilyMember.isEnabled = false
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
                    binding.btnEditFamilyMember.isEnabled = false
                    return
                }
                binding.etNote.error = ""
                checkSubmitButton()
            }
        })
    }

    /**
     * activates save button if all input is valid
     */
    private fun checkSubmitButton() {
        if (binding.etName.error.isNullOrBlank() && binding.etNewType.error.isNullOrBlank() && binding.etNote.error.isNullOrBlank()) {
            binding.btnEditFamilyMember.isEnabled = true
        }
    }

    /**
     * deletes profile image
     */
    private fun deleteProfileImage(): Boolean {
        return if (!familyMember.picturePath.isNullOrBlank() && FileSystemHandler.deleteFile(
                familyMember.picturePath!!
            )
        ) {
            Snackbar.make(binding.root, getText(R.string.file_deleted), Snackbar.LENGTH_SHORT)
                .show()
            binding.ivProfileImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_person_24))
            familyMember.picturePath = ""
            editFamilyMemberViewModel.saveFamilyMember(familyMember)
            true
        } else {

            Snackbar.make(binding.root, getString(R.string.no_image_deleted), Snackbar.LENGTH_SHORT)
                .show()

            false
        }
    }

    /**
     * Opens dialog to edit profile image or delete profile image
     */
    private fun editProfileImageDialog() {

        val alert = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_profileimage))
            .setMessage(getString(R.string.alert_new_image_deletes_old_image))
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.new_image)) { dialog, _ ->

                getNewProfileImageDialog()
                dialog.dismiss()
            }

        if (!familyMember.picturePath.isNullOrBlank()) {
            alert.setNegativeButton(getString(R.string.button_delete_picture)) { dialog, _ ->
                deleteProfileImage()
                dialog.dismiss()
            }
        }

        alert.show()
    }

    /**
     * Opens dialog to add new profile image
     */
    private fun getNewProfileImageDialog() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.take_profileimage))
            .setNeutralButton(getString(R.string.cancel)) { _dialog, _ ->
                _dialog.cancel()
            }
            .setNegativeButton(getString(R.string.gallery)) { _dialog, _ ->

                var result = true
                if (!familyMember.picturePath.isNullOrBlank()) {
                    result = deleteProfileImage()
                }
                if (result) {
                    profileImageGalleryListener()
                } else {

                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_image_could_not_be_taken),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                _dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.camera)) { _dialog, _ ->

                var result = true
                if (!familyMember.picturePath.isNullOrBlank()) {
                    result = deleteProfileImage()
                }
                if (result) {
                    profileImagePreviewClickListener()

                } else {

                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_image_could_not_be_taken),
                        Snackbar.LENGTH_SHORT
                    ).show()

                }
                _dialog.dismiss()
            }
            .show()
    }
}