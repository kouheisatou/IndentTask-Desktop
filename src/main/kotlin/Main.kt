import RootTaskModel.Resource.rootTask
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    Task(rootTask)
}


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        onKeyEvent = { event ->
            if(event.type == KeyEventType.KeyUp){
                when(event.key) {
                    Key.Enter -> rootTask.focusedTaskModel.value.createNewTask()
                    Key.DirectionRight -> rootTask.focusedTaskModel.value.indentRight()
                    Key.DirectionLeft -> rootTask.focusedTaskModel.value.indentLeft()
                    Key.DirectionUp -> rootTask.focusedTaskModel.value.moveFocusUp()
                    Key.DirectionDown -> rootTask.focusedTaskModel.value.moveFocusDown()
                    Key.PageUp -> rootTask.focusedTaskModel.value.moveUp()
                    Key.PageDown -> rootTask.focusedTaskModel.value.moveDown()
                    Key.Backspace -> rootTask.focusedTaskModel.value.remove()
                    Key.C -> rootTask.focusedTaskModel.value.isDone.value = !rootTask.focusedTaskModel.value.isDone.value
                }
            }
            true
        }
    ) {
        App()
    }
}
