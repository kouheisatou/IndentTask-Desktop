import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager

class RootTask : Task(null) {

    object Resource {
        val rootTask = RootTask()
        var focusedTask: Task? = null
        lateinit var focusManager: FocusManager
    }

    init {
        createNewTask()
    }

    override fun createNewTask() {
        childTasks.add(Task(this))
    }
}