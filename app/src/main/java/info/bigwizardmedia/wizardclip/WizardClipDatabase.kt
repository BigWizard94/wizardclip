package info.bigwizardmedia.wizardclip

import android.content.Context
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.Update
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long,
    val pinned: Boolean = false,
    val isSensitive: Boolean = false,
    val sensitiveKind: String? = null,
    val expiresAt: Long? = null,
    val source: String = "Manual",
)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val text: String,
    val createdAt: Long,
)

@Dao
interface ClipDao {
    @Query("SELECT * FROM clips ORDER BY pinned DESC, createdAt DESC")
    fun observeAll(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE text LIKE '%' || :query || '%' ORDER BY pinned DESC, createdAt DESC")
    fun observeSearch(query: String): Flow<List<ClipEntity>>

    @Query("SELECT COUNT(*) FROM clips")
    suspend fun count(): Int

    @Insert
    suspend fun insert(clip: ClipEntity): Long

    @Update
    suspend fun update(clip: ClipEntity)

    @Query("UPDATE clips SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clips")
    suspend fun deleteAll()

    @Query("DELETE FROM clips WHERE expiresAt IS NOT NULL AND expiresAt <= :now")
    suspend fun deleteExpired(now: Long)
}

@Dao
interface SnippetDao {
    @Query("SELECT * FROM snippets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SnippetEntity>>

    @Query("SELECT COUNT(*) FROM snippets")
    suspend fun count(): Int

    @Insert
    suspend fun insert(snippet: SnippetEntity): Long

    @Update
    suspend fun update(snippet: SnippetEntity)

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(entities = [ClipEntity::class, SnippetEntity::class], version = 1, exportSchema = false)
abstract class WizardClipDatabase : RoomDatabase() {
    abstract fun clipDao(): ClipDao
    abstract fun snippetDao(): SnippetDao

    companion object {
        fun create(context: Context): WizardClipDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                WizardClipDatabase::class.java,
                "wizardclip.db",
            ).setDriver(AndroidSQLiteDriver()).build()
        }
    }
}
