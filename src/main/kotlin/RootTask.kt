import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

class RootTask : Task(null) {
    init {
        createNewTask()
    }

    override fun createNewTask() {

        childTasks.add(Task(this))
    }

    @Composable
    override fun show() {
        Column {
            for(task in childTasks){
                task.show()
            }
        }
    }
}