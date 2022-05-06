package de.fhe.familycare.storage.repositories

import android.app.Application
import android.content.Context
import de.fhe.familycare.storage.core.FamilyCareDatabase
import de.fhe.familycare.storage.dao.ContactDao
import de.fhe.familycare.storage.model.Contact
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutionException

/**
 * Repository for Contacts
 */
class ContactRepository(context: Context) {
    private var _contactDao: ContactDao? = null

    private val contactDao get() = _contactDao!!

    init {
        val db : FamilyCareDatabase? = FamilyCareDatabase.getDatabase(context)
        _contactDao = db?.contactDao()
    }

    companion object {
        private var INSTANCE : ContactRepository? = null

        fun getRepository (application : Application) : ContactRepository?{
            if(INSTANCE == null) {
                synchronized(ContactRepository::class.java){
                    if (INSTANCE == null){
                        INSTANCE = ContactRepository(application)
                    }
                }
            }
            return INSTANCE
        }
    }

    /**
     * Function that gets all Contacts from database
     * @return a Flow of a List of Contacts
     */
    fun getAllContacts(): Flow<List<Contact>> {

        return contactDao.getAllContacts()
    }

    /**
     * Function to insert or update Contact in database
     * @param contact: The contact to be inserted
     * @return The ID of the new or updated contact. If something goes wrong, -1 is returned.
     */
    fun insertUpdateContact(contact: Contact):Long{
        try {
            return FamilyCareDatabase.executeWithReturn { contactDao.insertUpdateContact(contact) }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return -1
    }

    /**
     * Function to get the Contact with a given ID
     * @param contactId: The ID of the Contact
     * @return A Flow of Contact
     */
    fun getContactByID(contactId: Long): Flow<Contact>
    {
        return contactDao.getContactByID(contactId)
    }

    /**
     * delete contact by contactId
     *
     * @param contactId: The ID of the Contact
     */
    fun deleteContact(contactId: Long){
        try {
            return FamilyCareDatabase.execute{contactDao.deleteContact(contactId) }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}