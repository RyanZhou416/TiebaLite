package com.huanchengfly.tieba.post.models.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.huanchengfly.tieba.post.models.database.Account

@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    suspend fun getAll(): List<Account>

    @Query("SELECT * FROM account WHERE id = :accountId LIMIT 1")
    suspend fun getById(accountId: Int): Account?

    @Query("SELECT * FROM account WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): Account?

    @Query("SELECT * FROM account WHERE bduss = :bduss LIMIT 1")
    suspend fun getByBduss(bduss: String): Account?

    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Update
    fun updateSync(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Transaction
    suspend fun saveOrUpdate(account: Account) {
        val existing = getByUid(account.uid)
        if (existing != null) {
            update(account.copy(id = existing.id))
        } else {
            insert(account)
        }
    }

    @Query("SELECT * FROM account WHERE uid = :uid LIMIT 1")
    fun getByUidSync(uid: String): Account?

    @Query("SELECT * FROM account WHERE bduss = :bduss LIMIT 1")
    fun getByBdussSync(bduss: String): Account?

    @Query("SELECT * FROM account WHERE id = :accountId LIMIT 1")
    fun getByIdSync(accountId: Int): Account?

    @Query("SELECT * FROM account")
    fun getAllSync(): List<Account>

    @Delete
    fun deleteSync(account: Account)
}
