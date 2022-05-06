package de.fhe.familycare.storage.repositories

import android.app.Application
import android.content.Context
import de.fhe.familycare.storage.core.FamilyCareDatabase
import de.fhe.familycare.storage.dao.FamilyMemberTypeDao
import de.fhe.familycare.storage.model.FamilyMemberType
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutionException

/**
 * Repository for FamilyMemberTypes
 */
class FamilyMemberTypeRepository(context: Context) {

    private var _familyMemberTypeDao: FamilyMemberTypeDao? = null

    private val familyMemberTypeDao get() = _familyMemberTypeDao!!

    init {
        val db : FamilyCareDatabase? = FamilyCareDatabase.getDatabase(context)
        _familyMemberTypeDao = db?.familyMemberTypeDao()
    }

    companion object {
        private var INSTANCE : FamilyMemberTypeRepository? = null

        fun getRepository (application : Application) : FamilyMemberTypeRepository?{
            if(INSTANCE == null){
                synchronized(FamilyMemberTypeRepository::class.java){
                    if (INSTANCE == null) {
                        INSTANCE = FamilyMemberTypeRepository(application)
                    }
                }
            }
            return INSTANCE
        }
    }

    /**
     * Function that gets all FamilyMemberTypes from database
     * @return a Flow of a List of FamilyMemberTypes
     */
    fun getAllFamilyMemberTypes(): Flow<List<FamilyMemberType>> {
        return familyMemberTypeDao.getAllFamilyMemberTypes()
    }

    /**
     * Function to insert or update FamilyMemberType in database
     * @param name: The name of the FamilyMemberType to be inserted or updated
     * @return The ID of the new or updated FamilyMemberType. If something goes wrong, -1 is returned.
     */
    fun insertUpdateFamilyMemberType(name: String):Long{
        try {
            val familyMemberType = FamilyMemberType(name)
            return FamilyCareDatabase.executeWithReturn { familyMemberTypeDao.insertUpdateFamilyMemberType(familyMemberType) }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return -1
    }
}