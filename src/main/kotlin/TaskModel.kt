import RootTaskModel.Resource.rootTask
import RootTaskModel.Resource.tasksCount
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import java.util.Date

open class TaskModel(parent: TaskModel?) {

    var parent: TaskModel? = parent
        set(value) {
            depth = value?.depth?.plus(1) ?: 0
            field = value
        }
    val id = (tasksCount++)
    var content = mutableStateOf("")
    var isDone = mutableStateOf(false)
    var createdDate = Date()
    var childTaskModels: SnapshotStateList<TaskModel> = SnapshotStateList()
    var depth = 0
        set(value) {
            for (child in childTaskModels) {
                child.depth = value + 1
            }
            field = value
        }

    init {
        depth = if (parent == null) {
            0
        } else {
            parent.depth + 1
        }
    }

    fun getBottomTask(): TaskModel {
        return if (childTaskModels.isEmpty()) {
            this
        } else {
            println(childTaskModels.last().id)
            childTaskModels.last().getBottomTask()
        }
    }

    fun getAboveTask(): TaskModel? {
        val parent = parent ?: return null

        val aboveTaskIndex = parent.childTaskModels.indexOf(this) - 1
        if (aboveTaskIndex < 0) return null

        return parent.childTaskModels.getOrNull(aboveTaskIndex)
    }

    fun getBelowTask(): TaskModel? {
        val parent = parent ?: return null

        val belowTaskIndex = parent.childTaskModels.indexOf(this) + 1
        if (belowTaskIndex >= parent.childTaskModels.size) return null

        return parent.childTaskModels.getOrNull(belowTaskIndex)
    }

    open fun createNewTask() {
        val newTaskModel = TaskModel(parent)
        parent!!.childTaskModels.add(newTaskModel)
        rootTask.focusedTaskModel.value = newTaskModel
    }

    fun indentLeft() {
        val parent = parent!!

        val newParent = parent.parent ?: return

        val parentIndex = newParent.childTaskModels.indexOf(parent)

        this.parent = newParent
        newParent.childTaskModels.add(parentIndex + 1, this)

        parent.childTaskModels.remove(this)
    }

    fun indentRight() {
        val parent = parent!!

        val currentIndex = parent.childTaskModels.indexOf(this)
        if (currentIndex <= 0) return

        val newParent = parent.childTaskModels[currentIndex - 1]
        this.parent = newParent
        newParent.childTaskModels.add(this)

        parent.childTaskModels.remove(this)
    }

    private fun swap(targetTaskIndex: Int) {
        val parent = parent ?: return

        val temp = this
        parent.childTaskModels.remove(this)
        parent.childTaskModels.add(targetTaskIndex, temp)
    }

    fun moveUp() {
        swap(parent?.childTaskModels?.indexOf(getAboveTask()) ?: return)
    }

    fun moveDown() {
        swap(parent?.childTaskModels?.indexOf(getBelowTask()) ?: return)
    }

    fun remove() {
        val parent = parent ?: return
        if (parent is RootTaskModel && parent.childTaskModels.size == 1) return

        parent.childTaskModels.remove(this)

        rootTask.focusedTaskModel.value = if (parent is RootTaskModel) {
            childTaskModels[0]
        } else {
            parent
        }
    }

    override fun toString(): String {
        val result = java.lang.StringBuilder()
        for (i in 0 until depth) {
            result.append("    ")
        }
        result.append(
            "${
                if (rootTask.focusedTaskModel == this) {
                    "#"
                } else {
                    ""
                }
            }id=$id, parentId=${parent?.id}, depth=$depth, content=${content.value}, isDone=${isDone.value}\n"
        )

        for (child in childTaskModels) {
            result.append(child.toString())
        }
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        return (other is TaskModel && this.id == other.id)
    }

    fun isAllChildrenDone() {
        if (childTaskModels.isNotEmpty()) {
            var allDone = true

            childTaskModels.forEach {
                if (!it.isDone.value) {
                    allDone = false
                }
            }
            isDone.value = allDone
        }

        parent?.isAllChildrenDone()
    }

    fun moveFocusUp() {
        val parent = parent ?: return
        if (rootTask.focusedTaskModel.value != this) return

        val aboveTask = getAboveTask()
        rootTask.focusedTaskModel.value = if (aboveTask != null) {
            if (aboveTask.childTaskModels.isEmpty()) {
                aboveTask
            } else {
                aboveTask.getBottomTask()
            }
        } else {
            if (parent is RootTaskModel) {
                return
            } else {
                parent
            }
        }

    }

    fun moveFocusDown() {
        if (rootTask.focusedTaskModel.value != this) return

        rootTask.focusedTaskModel.value = if(childTaskModels.isEmpty()) {
            val belowTask = getBelowTask()
            if(belowTask != null){
                belowTask
            }else{
                var parent = parent ?: return
                while(parent.getBelowTask() == null){
                    parent = parent.parent ?: return
                }
                parent.getBelowTask() ?: return
            }
        }else{
            childTaskModels.first()
        }
    }

    override fun hashCode(): Int {
        var result = parent?.hashCode() ?: 0
        result = 31 * result + id
        result = 31 * result + content.hashCode()
        result = 31 * result + isDone.hashCode()
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + childTaskModels.hashCode()
        result = 31 * result + depth
        return result
    }
}