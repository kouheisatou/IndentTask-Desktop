interface TaskModelInterface {
    fun createNewTask(addToHistory: Boolean = true)
    fun cloneAndInsert(addToHistory: Boolean = true)
    fun indentLeft(addToHistory: Boolean = true)
    fun indentRight(addToHistory: Boolean = true)
    fun moveUp(addToHistory: Boolean = true)
    fun moveDown(addToHistory: Boolean = true)
    fun remove(addToHistory: Boolean = true)
    fun done(value: Boolean, checkParent: Boolean = true, addToHistory: Boolean = true)
    fun moveFocusUp(addToHistory: Boolean = true)
    fun moveFocusDown(addToHistory: Boolean = true)
}