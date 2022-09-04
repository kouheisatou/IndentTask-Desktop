import RootTaskModel.Resource.history
import RootTaskModel.Resource.rootTask
import RootTaskModel.Resource.tasksCount
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.focus.FocusRequester
import java.util.*

open class TaskModel(parent: TaskModel?) : TaskModelInterface {

    var parent: TaskModel? = parent
        set(value) {
            depth = value?.depth?.plus(1) ?: 0
            field = value
        }
    val id = (tasksCount++)
    var content = mutableStateOf("")
    var isDone = mutableStateOf(false)
    val createdDate = Calendar.getInstance()
    var childTaskModels: SnapshotStateList<TaskModel> = SnapshotStateList()
    val focusRequester = mutableStateOf(FocusRequester())
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

    override fun createNewTask(addToHistory: Boolean) {
        val newTaskModel = TaskModel(parent)
        createNewTask(newTaskModel)

        if (addToHistory) {
            println("add_to_history : createNewTask")
            history.add { createNewTask(false) }
        }
    }

    fun createNewTask(taskModel: TaskModel) {
        val belowTask = getBelowTask()
        if (belowTask != null) {
            val belowTaskIndex = parent!!.childTaskModels.indexOf(belowTask)
            parent!!.childTaskModels.add(belowTaskIndex, taskModel)
        } else {
            parent!!.childTaskModels.add(taskModel)
        }

        taskModel.done(value = false, checkParent = true, addToHistory = false)

        rootTask.focusedTaskModel.value = taskModel
    }

    fun clone(parent: TaskModel?): TaskModel {
        val clone = TaskModel(parent)
        clone.content.value = content.value
        clone.isDone.value = isDone.value

        childTaskModels.forEach {
            clone.childTaskModels.add(it.clone(clone))
        }

        return clone
    }

    override fun cloneAndInsert(addToHistory: Boolean) {
        createNewTask(rootTask.focusedTaskModel.value.clone(parent))

        if (addToHistory) {
            println("add_to_history : cloneAndInsert")
            history.add { createNewTask(false) }
        }
    }

    override fun indentLeft(addToHistory: Boolean) {
        val parent = parent!!

        val newParent = parent.parent ?: return

        val parentIndex = newParent.childTaskModels.indexOf(parent)

        this.parent = newParent
        newParent.childTaskModels.add(parentIndex + 1, this)

        parent.childTaskModels.remove(this)

        if (addToHistory) {
            println("add_to_history : indentLeft")
            history.add { indentLeft(false) }
        }
    }

    override fun indentRight(addToHistory: Boolean) {
        val parent = parent!!

        val currentIndex = parent.childTaskModels.indexOf(this)
        if (currentIndex <= 0) return

        val newParent = parent.childTaskModels[currentIndex - 1]
        this.parent = newParent
        newParent.childTaskModels.add(this)

        parent.childTaskModels.remove(this)

        if (addToHistory) {
            println("add_to_history : indentRight")
            history.add { indentRight(false) }
        }
    }

    private fun swap(targetTaskIndex: Int) {
        val parent = parent ?: return

        val temp = this
        parent.childTaskModels.remove(this)
        parent.childTaskModels.add(targetTaskIndex, temp)
    }

    override fun moveUp(addToHistory: Boolean) {
        val aboveTaskIndex = parent?.childTaskModels?.indexOf(getAboveTask()) ?: return
        if (aboveTaskIndex < 0) return
        swap(aboveTaskIndex)

        if (addToHistory) {
            println("add_to_history : moveUp")
            history.add { moveUp(false) }
        }
    }

    override fun moveDown(addToHistory: Boolean) {
        val belowTaskIndex = parent?.childTaskModels?.indexOf(getBelowTask()) ?: return
        if (belowTaskIndex < 0) return
        swap(belowTaskIndex)

        if (addToHistory) {
            println("add_to_history : moveDown")
            history.add { moveDown(false) }
        }
    }

    override fun remove(addToHistory: Boolean) {
        val parent = parent ?: return
        if (parent is RootTaskModel && parent.childTaskModels.size == 1) return

        rootTask.focusedTaskModel.value = if (parent is RootTaskModel) {
            getAboveTask() ?: parent.childTaskModels[0]
        } else {
            getAboveTask() ?: parent
        }

        parent.childTaskModels.remove(this)

        if (addToHistory) {
            println("add_to_history : remove")
            history.add { remove(false) }
        }
    }

    override fun toString(): String {
        val result = java.lang.StringBuilder()
        for (i in 0 until depth) {
            result.append("    ")
        }
        result.append(
            "${
                if (rootTask.focusedTaskModel.value == this) {
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

    override fun done(value: Boolean, checkParent: Boolean, addToHistory: Boolean) {
        isDone.value = value

        childTaskModels.forEach {
            it.done(value, checkParent = false, addToHistory = false)
        }

        if (checkParent) {
            var parent = parent ?: return
            while (parent !is RootTaskModel) {
                parent.isDone.value = parent.isAllChildrenDone()
                parent = parent.parent ?: break
            }
        }

        if (addToHistory) {
            println("add_to_history : done")
            history.add { done(value, checkParent, false) }
        }
    }

    private fun isAllChildrenDone(): Boolean {
        var allDone = true

        childTaskModels.forEach {
            if (!it.isDone.value) {
                allDone = false
            }
        }
        parent?.isAllChildrenDone()

        return allDone
    }

    override fun moveFocusUp(addToHistory: Boolean) {
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

        if (addToHistory) {
            println("add_to_history : moveFocusUp")
            history.add { moveFocusUp(false) }
        }
    }

    override fun moveFocusDown(addToHistory: Boolean) {
        if (rootTask.focusedTaskModel.value != this) return

        rootTask.focusedTaskModel.value = if (childTaskModels.isEmpty()) {
            val belowTask = getBelowTask()
            if (belowTask != null) {
                belowTask
            } else {
                var parent = parent ?: return
                while (parent.getBelowTask() == null) {
                    parent = parent.parent ?: return
                }
                parent.getBelowTask() ?: return
            }
        } else {
            childTaskModels.first()
        }

        if (addToHistory) {
            println("add_to_history : moveFocusDown")
            history.add { moveFocusDown(false) }
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