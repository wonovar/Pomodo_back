import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM tasks WHERE date = :date")
    suspend fun getTasksByDate(date: String): List<Task>

    @Delete
    suspend fun deleteTask(task: Task)
}