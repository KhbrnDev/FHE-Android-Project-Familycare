package de.fhe.familycare.view.contact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.fhe.familycare.storage.model.Contact
import de.fhe.familycare.storage.repositories.ContactRepository

/**
 * View Model for all Contact-Related Views
 */
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val _contactRepository = ContactRepository.getRepository(application)
    private val contactRepository get() = _contactRepository!!
    val allContacts: LiveData<List<Contact>> = contactRepository.getAllContacts().asLiveData()

    fun saveContact(newContact: Contact): Long {
        return contactRepository.insertUpdateContact(newContact)
    }

    fun getContact(id: Long): LiveData<Contact> {
        return contactRepository.getContactByID(id).asLiveData()
    }

    fun deleteContact(contactId: Long){

        contactRepository.deleteContact(contactId)
    }
}
