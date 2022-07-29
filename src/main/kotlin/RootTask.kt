import RootTask.Resource.focusedTask

class RootTask : Task(null) {

    object Resource {
        val rootTask = RootTask()
        var focusedTask: Task? = null
            set(value) {
                field = value
                focusSelectedTask()
            }

        fun focusSelectedTask(){
            focusedTask ?: return
            focusedTask!!.focusRequester.value.requestFocus()
            println("focused on ${focusedTask?.id}")
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