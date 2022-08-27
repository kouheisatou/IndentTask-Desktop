import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class RootTaskModel : TaskModel(null) {

    object Resource {
        var tasksCount = 0
        val rootTask = RootTaskModel()
    }

    var focusedTaskModel: MutableState<TaskModel>
    val text = mutableStateOf("")

    init {
        createNewTask()
        focusedTaskModel = mutableStateOf(childTaskModels[0])
    }

    override fun createNewTask() {
        childTaskModels.add(TaskModel(this))
    }

    override fun toString(): String {
        return "focusedTaskId=${focusedTaskModel.value.id}, " + super.toString()
    }
}