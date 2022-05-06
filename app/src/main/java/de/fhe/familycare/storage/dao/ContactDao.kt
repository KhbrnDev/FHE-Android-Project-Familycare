package de.fhe.familycare.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.fhe.familycare.storage.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("DELETE FROM CONTACT")
    fun deleteAll()

    @Query("SELECT * FROM Contact ORDER BY contact.name asc")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUpdateContact(contact: Contact) : Long

    @Query("SELECT * FROM Contact WHERE id = :contactId")
    fun getContactByID(contactId: Long): Flow<Contact>

    @Query("DELETE FROM contact WHERE contact.id = :contactId")
    fun deleteContact(contactId: Long)
}