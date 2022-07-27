import Task.Count.count
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Date

open class Task(protected var parent: Task?) {

    object Count{
        var count = 0
    }

//    constructor(parent: Task?){
//        this.parent = parent
//    }
//    protected var parent: Task? = null
//        set(value) {
//            println("${parent?.id}, ${parent?.depth}")
//            depth = parent?.depth?.plus(1) ?: 0
//            field = value
//        }
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
        depth = if (parent == null) {
            0
        }else{
            parent!!.depth + 1
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
        this.depth = newParent.depth + 1
        newParent.childTasks.add(parentIndex+1, this)

        parent.childTasks.remove(this)
    }

    fun indentRight(){
        val parent = parent!!

        val currentIndex = parent.childTasks.indexOf(this)
        if(currentIndex <= 0) return

        val newParent = parent.childTasks[currentIndex-1]
        this.parent = newParent
        this.depth = newParent.depth + 1
        newParent.childTasks.add(this)

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

    @Composable
    open fun show(){
        Row(modifier = Modifier.padding(start = ((depth-1)*20).dp)) {
            Checkbox(
                checked = isDone.value,
                onCheckedChange = {
                    isDone.value = it
                },
            )
            OutlinedTextField(
                value = content.value,
                onValueChange = {
                    content.value = it
                },
            )
            Button(
                onClick = {
                    createNewTask()
                },
            ){
                Text("newTask")
            }
            Button(
                onClick = {
                    indentLeft()
                    println(rootTask.toString())
                },
            ){
                Text("<")
            }
            Button(
                onClick = {
                    indentRight()
                    println(rootTask.toString())
                },
            ){
                Text(">")
            }
        }

        Column {
            for(task in childTasks){
                task.show()
            }
        }
    }
}