package de.fhe.familycare.storage.dao

import androidx.room.*
import de.fhe.familycare.storage.model.WeightData
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDataDao {

    @Query("SELECT * FROM weightdata WHERE weightdata.familyMemberId = :familyMemberId")
    fun getAllWeightByFamilyMemberID(familyMemberId: Long): Flow<List<WeightData>>

    @Query("SELECT * FROM weightdata WHERE weightdata.familyMemberId = :familyMemberId order by weightdata.date DESC LIMIT 1")
    fun getLastWeightByFamilyMemberID(familyMemberId: Long): Flow<WeightData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUpdateWeightData(weightData: WeightData) : Long

    @Query("DELETE FROM WEIGHTDATA")
    fun deleteAll()

}