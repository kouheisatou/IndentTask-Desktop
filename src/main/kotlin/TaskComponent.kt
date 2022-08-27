import RootTaskModel.Resource.rootTask
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Task(taskModel: TaskModel) {

    // when compose succseeded
    SideEffect {
        if(rootTask.focusedTaskModel.value == taskModel){
            taskModel.focusRequester.value.requestFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(
            if (rootTask.focusedTaskModel.value == taskModel) {
                Color.LightGray
            } else {
                Color.Transparent
            }
        )
    ) {
        // ----indent----
        // top level task
        val modifier = Modifier.padding(start = ((taskModel.depth - 1) * 30).dp)
        if (taskModel.parent is RootTaskModel) {
            Text("", modifier = modifier)
        }
        // if task is last of parent
        else if (
            taskModel.parent?.childTaskModels?.indexOf(taskModel)
            == (taskModel.parent?.childTaskModels?.size ?: -1) - 1
        ) {
            Text("┗", modifier = modifier)
        } else {
            Text("┣", modifier = modifier)
        }

        Checkbox(
            modifier = Modifier.focusable(false),
            checked = taskModel.isDone.value,
            onCheckedChange = {
                taskModel.done(it)
            },
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusable(false)
                .onFocusChanged {
                    if(it.isFocused){
                        rootTask.focusedTaskModel.value = taskModel
                    }
                }
                .focusRequester(focusRequester = taskModel.focusRequester.value),
            value = taskModel.content.value,
            onValueChange = {
                taskModel.content.value = it
            },
            label = { Text("${taskModel.id}:${taskModel.parent?.id}") },
            maxLines = 1,
        )
    }

    taskModel.childTaskModels.forEach {
        Task(it)
    }
}


@Composable
fun Task(task: RootTaskModel) {
    LazyColumn {
        items(task.childTaskModels.size) { index ->
            Task(task.childTaskModels[index])
        }
    }
}
