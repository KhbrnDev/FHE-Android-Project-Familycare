package de.fhe.familycare.storage.dao

import androidx.room.*
import de.fhe.familycare.storage.model.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {

    @Query("SELECT * FROM Familymember WHERE id = :familyMemberId")
    fun getFamilyMemberByID(familyMemberId: Long): Flow<FamilyMember>

    @Query("SELECT * FROM Familymember")
    fun getAllFamilyMember(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM Familymember WHERE isActive = 1")
    fun getAllActiveFamilyMembers(): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUpdateFamilyMember(familyMember: FamilyMember) : Long

    @Query("DELETE FROM FAMILYMEMBER")
    fun deleteAll()

}