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
//    TextField(
//        modifier = Modifier
//            .fillMaxSize()
//            .onKeyEvent { event ->
//                if (event.type == KeyEventType.KeyUp) {
//                    when (event.key) {
//                        Key.Tab -> {
//                            if(event.isShiftPressed){
//                                rootTask.focusedTaskModel.indentLeft()
//                            }else{
//                                rootTask.focusedTaskModel.indentRight()
//                            }
//                        }
//                        Key.Enter -> {
//                            rootTask.focusedTaskModel.createNewTask()
//                        }
//                        Key.DirectionDown -> {
//                            if(event.isAltPressed){
//                                rootTask.focusedTaskModel.moveDown()
//                            }else{
//                                rootTask.focusedTaskModel.moveFocusDown()
//                            }
//                        }
//                        Key.DirectionUp -> {
//                            if(event.isAltPressed){
//                                rootTask.focusedTaskModel.moveUp()
//                            }else{
//                                rootTask.focusedTaskModel.moveFocusUp()
//                            }
//                        }
//                        Key.Delete -> {
//                            if(event.isShiftPressed){
//                                rootTask.focusedTaskModel.remove()
//                            }
//                        }
//                    }
//                }
//                false
//            },
//        value = rootTask.toString(),
//
//        onValueChange = { },
//    )
}


@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
        MenuBar {
            Menu("test", mnemonic = 'F') {
                Item(
                    "focusUp",
                    onClick = { rootTask.focusedTaskModel.value.moveFocusUp() },
                    shortcut = KeyShortcut(Key.DirectionUp)
                )
                Item(
                    "focusDown",
                    onClick = { rootTask.focusedTaskModel.value.moveFocusDown() },
                    shortcut = KeyShortcut(Key.DirectionDown)
                )
                Item(
                    "moveUp",
                    onClick = { rootTask.focusedTaskModel.value.moveUp() },
                    shortcut = KeyShortcut(Key.DirectionUp, alt = true)
                )
                Item(
                    "moveDown",
                    onClick = { rootTask.focusedTaskModel.value.moveDown() },
                    shortcut = KeyShortcut(Key.DirectionDown, alt = true)
                )
                Item(
                    "new",
                    onClick = { rootTask.focusedTaskModel.value.createNewTask() },
                    shortcut = KeyShortcut(Key.Enter, alt = true)
                )
                Item(
                    "indentRight",
                    onClick = { rootTask.focusedTaskModel.value.indentRight() },
                    shortcut = KeyShortcut(Key.DirectionRight)
                )
                Item(
                    "indentLeft",
                    onClick = { rootTask.focusedTaskModel.value.indentLeft() },
                    shortcut = KeyShortcut(Key.DirectionLeft)
                )
                Item(
                    "delete",
                    onClick = { rootTask.focusedTaskModel.value.remove() },
                    shortcut = KeyShortcut(Key.Backspace, alt = true)
                )
            }
        }
    }
}
