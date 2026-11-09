package com.smartalerter.notification.data.db

import androidx.room.*
import com.smartalerter.notification.data.model.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM alert_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM alert_rules WHERE packageName = :packageName AND isEnabled = 1")
    suspend fun getEnabledRulesForPackage(packageName: String): List<RuleEntity>

    @Query("SELECT * FROM alert_rules WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: Long): RuleEntity?

    @Query("SELECT COUNT(*) FROM alert_rules WHERE packageName = :packageName AND isEnabled = 1")
    fun getActiveRuleCountForPackage(packageName: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity): Long

    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Delete
    suspend fun deleteRule(rule: RuleEntity)

    @Query("UPDATE alert_rules SET isEnabled = :isEnabled WHERE id = :ruleId")
    suspend fun toggleRuleEnabled(ruleId: Long, isEnabled: Boolean)
}