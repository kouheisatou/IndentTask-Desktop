import RootTask.Resource.focusedTask

class RootTask : Task(null) {

    object Resource {
        val rootTask = RootTask()
        var focusedTask: Task? = null

        fun focusSelectedTask(){
            focusedTask ?: return
            focusedTask!!.focusRequester.value.requestFocus()
        }
    }

    init {
        createNewTask()
    }

    override fun createNewTask() {
        childTasks.add(Task(this))
    }

    override fun toString(): String {
        return "focusedTaskId=${focusedTask?.id}, " + super.toString()
    }
}