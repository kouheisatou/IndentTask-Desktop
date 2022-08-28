import RootTaskModel.Resource.rootTask
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

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
            if (event.type == KeyEventType.KeyUp) {
                when (event.key) {
                    Key.Enter -> if (event.isMetaPressed) {
                        rootTask.focusedTaskModel.value.done(!rootTask.focusedTaskModel.value.isDone.value)
                    } else {
                        rootTask.focusedTaskModel.value.createNewTask()
                    }
                    Key.Tab -> {
                        if (event.isShiftPressed) {
                            rootTask.focusedTaskModel.value.indentLeft()
                        } else {
                            rootTask.focusedTaskModel.value.indentRight()
                        }
                    }
                    Key.DirectionUp -> {
                        if (event.isMetaPressed) {
                            rootTask.focusedTaskModel.value.moveUp()
                        } else {
                            rootTask.focusedTaskModel.value.moveFocusUp()
                        }
                    }
                    Key.DirectionDown -> {
                        if (event.isMetaPressed) {
                            rootTask.focusedTaskModel.value.moveDown()
                        } else {
                            rootTask.focusedTaskModel.value.moveFocusDown()
                        }
                    }
                    Key.Backspace -> {
                        if (event.isMetaPressed) {
                            rootTask.focusedTaskModel.value.remove()
                        }
                    }
                    Key.D -> {
                        if (event.isMetaPressed) {
                            rootTask.focusedTaskModel.value.createNewTask(rootTask.focusedTaskModel.value.clone())
                        }
                    }
                }
            }
            true
        }
    ) {
        App()
    }
}
