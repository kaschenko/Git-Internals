package gitinternals


fun main() {
    println("Enter .git directory location:")
    val input = readLine()!!.toString().replace("/", "\\")
    println("Enter command:")
    val command = readLine()!!.toString()
    when(command) {
        "list-branches" -> listBranches()
        "cat-file" -> getObject(input)
        "log" -> log()
        "commit-tree" -> commitTree()
    }


}
