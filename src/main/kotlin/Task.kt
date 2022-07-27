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
import androidx.compose.ui.unit.dp
import java.util.Date

open class Task(parent: Task?) {

    protected var parent: Task? = parent
        set(value) {
            depth = value?.depth?.plus(1) ?: 0
            field = value
        }
    protected val id = (count++)
    protected var content = mutableStateOf("")
    protected var isDone = mutableStateOf(false)
    protected var createdDate = Date()
    protected val childTasks = mutableStateListOf<Task>()
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
        parent!!.childTasks.add(Task(parent))
    }

    fun indentLeft(){
        val parent = parent!!

        val newParent = parent.parent ?: return

        val parentIndex = newParent.childTasks.indexOf(parent)

        this.parent = newParent
        newParent.childTasks.add(parentIndex+1, this)

        parent.childTasks.remove(this)
    }

    fun indentRight(){
        val parent = parent!!

        val currentIndex = parent.childTasks.indexOf(this)
        if(currentIndex <= 0) return

        val newParent = parent.childTasks[currentIndex-1]
        this.parent = newParent
        newParent.childTasks.add(this)

        parent.childTasks.remove(this)
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
    }

    fun moveDown(){
        val parent = parent ?: return

        val belowTaskIndex = parent.childTasks.indexOf(this) +1
        if(belowTaskIndex >= parent.childTasks.size) return


        swap(belowTaskIndex)
    }

    fun remove(){
        val parent = parent ?: return

        if(parent is RootTask && parent.childTasks.size == 1) return
        parent.childTasks.remove(this)
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

    fun isAllChildrenDone(): Boolean{
        childTasks.forEach {
            if(!it.isDone.value){
                return false
            }
        }
        return true
    }

    object TaskFactory{
        var count = 0

        @Composable
        fun Task(task: Task){

            Row(verticalAlignment = Alignment.CenterVertically) {
                // ----indent----
                val modifier = Modifier.padding(horizontal = ((task.depth-1)*20).dp)
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

                        task.parent?.isDone?.value = task.parent?.isAllChildrenDone() == true
                    },
                )
                OutlinedTextField(
                    value = task.content.value,
                    onValueChange = {
                        task.content.value = it
                    },
                    label = { Text("${task.id}:${task.parent?.id}")}
                )
                Button(
                    onClick = {
                        task.createNewTask()
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
            }

            Column {
                task.childTasks.forEach {
                    Task(it)
                }
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