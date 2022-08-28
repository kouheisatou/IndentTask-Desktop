import RootTaskModel.Resource.rootTask
import RootTaskModel.Resource.tasksCount
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.focus.FocusRequester
import java.util.*

open class TaskModel(parent: TaskModel?) {

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

    open fun createNewTask() {
        val newTaskModel = TaskModel(parent)
        createNewTask(newTaskModel)
    }

    open fun createNewTask(taskModel: TaskModel) {
        val belowTask = getBelowTask()
        if (belowTask != null) {
            val belowTaskIndex = parent!!.childTaskModels.indexOf(belowTask)
            parent!!.childTaskModels.add(belowTaskIndex, taskModel)
        } else {
            parent!!.childTaskModels.add(taskModel)
        }

        taskModel.done(value = false, checkParent = true)
        rootTask.focusedTaskModel.value = taskModel
    }

    fun clone(parent: TaskModel? = this.parent): TaskModel {
        val clone = TaskModel(parent)
        clone.content.value = content.value
        clone.isDone.value = isDone.value

        childTaskModels.forEach {
            clone.childTaskModels.add(it.clone(clone))
        }

        return clone
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
        val aboveTaskIndex = parent?.childTaskModels?.indexOf(getAboveTask()) ?: return
        if (aboveTaskIndex < 0) return
        swap(aboveTaskIndex)
    }

    fun moveDown() {
        val belowTaskIndex = parent?.childTaskModels?.indexOf(getBelowTask()) ?: return
        if (belowTaskIndex < 0) return
        swap(belowTaskIndex)
    }

    fun remove() {
        val parent = parent ?: return
        if (parent is RootTaskModel && parent.childTaskModels.size == 1) return

        rootTask.focusedTaskModel.value = if (parent is RootTaskModel) {
            getAboveTask() ?: parent.childTaskModels[0]
        } else {
            getAboveTask() ?: parent
        }

        parent.childTaskModels.remove(this)

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

    fun done(value: Boolean, checkParent: Boolean = true) {
        isDone.value = value

        childTaskModels.forEach {
            it.done(value, false)
        }

        if (checkParent) {
            var parent = parent ?: return
            while (parent !is RootTaskModel) {
                parent.isDone.value = parent.isAllChildrenDone()
                parent = parent.parent ?: break
            }
        }
    }

    fun isAllChildrenDone(): Boolean {
        var allDone = true

        childTaskModels.forEach {
            if (!it.isDone.value) {
                allDone = false
            }
        }
        parent?.isAllChildrenDone()

        return allDone
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