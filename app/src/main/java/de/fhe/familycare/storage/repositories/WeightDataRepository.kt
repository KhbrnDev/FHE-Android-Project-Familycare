package de.fhe.familycare.storage.repositories

import android.app.Application
import android.content.Context
import de.fhe.familycare.storage.core.FamilyCareDatabase
import de.fhe.familycare.storage.dao.WeightDataDao
import de.fhe.familycare.storage.model.WeightData
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutionException

/**
 * Repository for WeightData
 */
class WeightDataRepository (context: Context) {

    private var _weightDataDao: WeightDataDao? = null

    private val weightDataDao get() = _weightDataDao!!

    init {
        val db : FamilyCareDatabase? = FamilyCareDatabase.getDatabase(context)
        _weightDataDao = db?.weightDataDao()
    }

    companion object {
        private var INSTANCE : WeightDataRepository? = null

        fun getRepository ( application : Application) : WeightDataRepository? {
            if(INSTANCE == null) {
                synchronized(WeightDataRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = WeightDataRepository(application)
                    }
                }
            }
            return INSTANCE
        }
    }

    /**
     * Function to get latest WeightData of given FamilyMember
     * @param familyMemberId: ID of the FamilyMember
     * @return Flow of WeightData
     */
    fun getLastWeightByFamilyMemberID(familyMemberId: Long): Flow<WeightData> {
        return weightDataDao.getLastWeightByFamilyMemberID(familyMemberId)
    }

    /**
     * Function that gets all WeightData of given FamilyMember from database
     * @param familyMemberId: The ID of the FamilyMember
     * @return a Flow of a List of WeightData
     */
    fun getAllWeightByFamilyMemberID(familyMemberId: Long): Flow<List<WeightData>> {
        return weightDataDao.getAllWeightByFamilyMemberID(familyMemberId)
    }

    /**
     * Function to insert or update WeightData into database
     * @param weightData: WeightData Object to be inserted
     * @return The ID of the new or updated WeightData. If something goes wrong, -1 is returned.
     */
    fun insertUpdateWeightData(weightData: WeightData):Long
    {
        return try {
            FamilyCareDatabase.executeWithReturn {weightDataDao.insertUpdateWeightData(weightData)}
        } catch (e: ExecutionException){
            -1
        }
    }
}