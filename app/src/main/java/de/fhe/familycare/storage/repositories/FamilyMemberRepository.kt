package de.fhe.familycare.storage.repositories

import android.app.Application
import android.content.Context
import de.fhe.familycare.storage.core.FamilyCareDatabase
import de.fhe.familycare.storage.dao.FamilyMemberDao
import de.fhe.familycare.storage.model.FamilyMember
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutionException

/**
 * Repository for FamilyMembers
 */
class FamilyMemberRepository(context: Context) {

    private var _familyMemberDao: FamilyMemberDao? = null

    private val familyMemberDao get() = _familyMemberDao!!

    init {
        val db : FamilyCareDatabase? = FamilyCareDatabase.getDatabase(context)
        _familyMemberDao = db?.familyMemberDao()
    }

    companion object {
        private var INSTANCE : FamilyMemberRepository? = null

        fun getRepository ( application : Application) : FamilyMemberRepository? {
            if(INSTANCE == null) {
                synchronized(FamilyMemberRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = FamilyMemberRepository(application)
                    }
                }
            }
            return INSTANCE
        }
    }

    /**
     * Function to get the FamilyMember with a given ID
     * @param familyMemberId: The ID of the FamilyMember
     * @return A Flow of FamilyMember
     */
    fun getFamilyMemberByID(familyMemberId: Long): Flow<FamilyMember> {
        return familyMemberDao.getFamilyMemberByID(familyMemberId)
    }

    /**
     * Function that gets all FamilyMembers from database
     * @return a Flow of a List of FamilyMembers
     */
    fun getAllFamilyMember(): Flow<List<FamilyMember>> {
        return familyMemberDao.getAllFamilyMember()
    }

    /**
     * Function that gets all FamilyMembers from database with isActive == true
     * @return a Flow of a List of FamilyMembers
     */
    fun getAllActiveFamilyMembers(): Flow<List<FamilyMember>> {
        return familyMemberDao.getAllActiveFamilyMembers()
    }

    /**
     * Function to insert or update FamilyMember in database
     * @param familyMember: The FamilyMember to be inserted
     * @return The ID of the new or updated FamilyMember. If something goes wrong, -1 is returned.
     */
    fun insertUpdateFamilyMember(familyMember: FamilyMember):Long{
        try {
            return FamilyCareDatabase.executeWithReturn { familyMemberDao.insertUpdateFamilyMember(familyMember) }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return -1
    }
}