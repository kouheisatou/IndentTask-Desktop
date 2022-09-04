import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class RootTaskModel : TaskModel(null) {

    object Resource {
        var tasksCount = 0
        var rootTask = RootTaskModel()

        val history = mutableListOf<() -> Unit>()
        var undoCount = 0

        fun undo() {
            rootTask.childTaskModels.removeAll { true }
            rootTask.createNewTask(false)
            rootTask.focusedTaskModel = mutableStateOf(rootTask.childTaskModels[0])
            tasksCount = 0

            undoCount++
            println(history)
            for (i in 0 until history.size - undoCount) {
                history[i].invoke()
            }
        }
    }

    var focusedTaskModel: MutableState<TaskModel>

    init {
        createNewTask(false)
        focusedTaskModel = mutableStateOf(childTaskModels[0])
    }

    override fun createNewTask(addToHistory: Boolean) {
        childTaskModels.add(TaskModel(this))
    }

    override fun toString(): String {
        return "focusedTaskId=${focusedTaskModel.value.id}, " + super.toString()
    }
}