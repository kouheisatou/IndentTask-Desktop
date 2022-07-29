import RootTask.Resource.focusSelectedTask
import RootTask.Resource.focusedTask
import RootTask.Resource.rootTask
import Task.TaskFactory.count
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import java.util.Date

open class Task(parent: Task?) {

    protected var parent: Task? = parent
        set(value) {
            depth = value?.depth?.plus(1) ?: 0
            field = value
        }
    val id = (count++)
    protected var content = mutableStateOf("")
    protected var isDone = mutableStateOf(false)
    protected var createdDate = Date()
    protected val childTasks = mutableStateListOf<Task>()
    val focusRequester = mutableStateOf(FocusRequester())
    protected var depth = 0
        set(value) {
            for(child in childTasks){
                child.depth = value + 1
            }
            field = value
        }

    init {
        this.depth = if (parent == null) {
            0
        }else{
            parent.depth + 1
        }
    }

    open fun createNewTask(){
        val newTask = Task(parent)
        parent!!.childTasks.add(newTask)
    }

    fun indentLeft(){
        val parent = parent!!

        val newParent = parent.parent ?: return

        val parentIndex = newParent.childTasks.indexOf(parent)

        this.parent = newParent
        newParent.childTasks.add(parentIndex+1, this)

        parent.childTasks.remove(this)

        focusedTask?.focusRequester?.value?.requestFocus()
    }

    fun indentRight(){
        val parent = parent!!

        val currentIndex = parent.childTasks.indexOf(this)
        if(currentIndex <= 0) return

        val newParent = parent.childTasks[currentIndex-1]
        this.parent = newParent
        newParent.childTasks.add(this)

        parent.childTasks.remove(this)

        focusedTask?.focusRequester?.value?.requestFocus()
    }

    private fun swap(targetTaskIndex: Int){
        val parent = parent ?: return

        val temp = this
        parent.childTasks.remove(this)
        parent.childTasks.add(targetTaskIndex, temp)
    }

    fun moveUp(){
        val parent = parent ?: return

        val aboveTaskIndex = parent.childTasks.indexOf(this) -1
        if(aboveTaskIndex < 0) return

        swap(aboveTaskIndex)

        focusedTask?.focusRequester?.value?.requestFocus()
    }

    fun moveDown(){
        val parent = parent ?: return

        val belowTaskIndex = parent.childTasks.indexOf(this) +1
        if(belowTaskIndex >= parent.childTasks.size) return

        swap(belowTaskIndex)

        focusedTask?.focusRequester?.value?.requestFocus()
    }

    fun remove(){
        val parent = parent ?: return

        if(parent is RootTask && parent.childTasks.size == 1) return
        parent.childTasks.remove(this)
        focusedTask = null
    }

    override fun toString(): String {
        val result = java.lang.StringBuilder()
        for(i in 0 until depth){
            result.append("    ")
        }
        result.append("id=$id, parentId=${parent?.id}, depth=$depth, content=${content.value}, isDone=${isDone.value}\n")

        for(child in childTasks){
            result.append(child.toString())
        }
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        return this.id == (other as Task).id
    }

    fun isAllChildrenDone(){
        if(childTasks.isNotEmpty()){
            var allDone = true

            childTasks.forEach {
                if(!it.isDone.value){
                    allDone = false
                }
            }
            isDone.value = allDone
        }

        parent?.isAllChildrenDone()
    }

    fun moveFocusUp(){
        val parent = parent ?: return
        if(focusedTask != this) return

        val aboveTaskIndex = parent.childTasks.indexOf(this) -1
        if(aboveTaskIndex < 0) return

        focusedTask = parent.childTasks[aboveTaskIndex]
    }

    fun moveFocusDown(){
        val parent = parent ?: return
        if(focusedTask != this) return

        val belowTaskIndex = parent.childTasks.indexOf(this) +1
        if(belowTaskIndex >= parent.childTasks.size) return

        focusedTask = parent.childTasks[belowTaskIndex]
    }

    override fun hashCode(): Int {
        var result = parent?.hashCode() ?: 0
        result = 31 * result + id
        result = 31 * result + content.hashCode()
        result = 31 * result + isDone.hashCode()
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + childTasks.hashCode()
        result = 31 * result + focusRequester.hashCode()
        result = 31 * result + depth
        return result
    }

    object TaskFactory{
        var count = 0

        @Composable
        fun Task(task: Task){

            // when compose succseeded
            SideEffect {
                task.parent?.isAllChildrenDone()
                focusSelectedTask()
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // ----indent----
                val modifier = Modifier.padding(start = ((task.depth-1)*30).dp)
                if(task.parent is RootTask){
                    Text("", modifier = modifier)
                }
                // if task is last of parent
                else if(task.parent?.childTasks?.indexOf(task) == (task.parent?.childTasks?.size ?: -1)-1){
                    Text("┗", modifier = modifier)
                }
                else{
                    Text("┣", modifier = modifier)
                }

                Checkbox(
                    checked = task.isDone.value,
                    onCheckedChange = {
                        task.isDone.value = it
                        println(rootTask.toString())
                    },
                )
                OutlinedTextField(
                    value = task.content.value,
                    onValueChange = {
                        task.content.value = it
                    },
                    label = { Text("${task.id}:${task.parent?.id}")},
                    modifier = Modifier
                        .onFocusChanged {
                            if(it.isFocused){
                                focusedTask = task
                            }
                        }
                        .focusRequester(focusRequester = task.focusRequester.value)
                )
                Button(
                    onClick = {
                        task.createNewTask()
                        println(rootTask.toString())
                    },
                ){
                    Text("new")
                }
                Button(
                    onClick = {
                        task.remove()
                        println(rootTask.toString())
                    },
                ){
                    Text("rem")
                }
                Button(
                    onClick = {
                        task.indentLeft()
                        println(rootTask.toString())
                    },
                ){
                    Text("<")
                }
                Button(
                    onClick = {
                        task.indentRight()
                        println(rootTask.toString())
                    },
                ){
                    Text(">")
                }
                Button(
                    onClick = {
                        task.moveUp()
                        println(rootTask.toString())
                    },
                ){
                    Text("^")
                }
                Button(
                    onClick = {
                        task.moveDown()
                        println(rootTask.toString())
                    },
                ){
                    Text("v")
                }
                Button(
                    onClick = {
                        task.moveFocusUp()
                        println(rootTask.toString())
                    },
                ){
                    Text("focus^")
                }
                Button(
                    onClick = {
                        task.moveFocusDown()
                        println(rootTask.toString())
                    },
                ){
                    Text("focusv")
                }
            }

            task.childTasks.forEach {
                Task(it)
            }
        }



        @Composable
        fun Task(task: RootTask){
            Column {
                task.childTasks.forEach {
                    Task(it)
                }
            }
        }
    }
}