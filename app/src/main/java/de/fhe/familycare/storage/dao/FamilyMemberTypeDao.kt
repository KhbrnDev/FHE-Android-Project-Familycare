package de.fhe.familycare.storage.dao

import androidx.room.*
import de.fhe.familycare.storage.model.FamilyMemberType
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberTypeDao {

    @Query("SELECT * FROM FamilyMemberType WHERE name = :familyMemberTypeName")
    fun getFamilyMemberTypeByName(familyMemberTypeName: String): Flow<FamilyMemberType>

    @Query("SELECT * FROM FamilyMemberType")
    fun getAllFamilyMemberTypes(): Flow<List<FamilyMemberType>>

    @Query("SELECT name FROM FamilyMemberType")
    fun getAllTypes(): Flow<List<String>>

    @Insert
    fun insertUpdateFamilyMemberType(familyMemberType: FamilyMemberType) : Long

    @Delete
    fun deleteFamilyMemberType(familyMemberType: FamilyMemberType)

    @Query("DELETE FROM FamilyMemberType")
    fun deleteAll()

}