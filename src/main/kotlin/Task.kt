import androidx.compose.runtime.Composable
import java.util.Date

class Task(private val parent: Task?) {

    private val content = ""
    private val isDone = false
    private val createdDate = Date()
    private val childTasks = mutableListOf<Task>()

    fun createNewTask(){
        
    }

    fun indentUp(){

    }

    fun indentDown(){

    }

    @Composable
    fun view(){

    }
}