package gitinternals

import java.io.File
import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

fun listBranches() {
    val path = """Git Internals\task\test\refs\heads\"""
    val file = File(path)
    val head = getHead()
    file.walk().forEach { if (it.name == head) println("* ${it.name}") else if (it.isFile) println("  ${it.name}") }
}
fun getHead(): String {
    val path = """Git Internals\task\test\HEAD"""
    val file = File(path)
    val decode = file.readLines()
    val head = decode[0].substringAfterLast("/")
    return head
}
fun log() {
    println("Enter branch name:")
    val branch = readLine()!!.toString()
    val path = """Git Internals\task\test\refs\heads\$branch"""
    val file = File(path).bufferedReader()
    val hashNum = file.readLines()[0]
    getLogObject(hashNum)
}

fun getLogObject(hashNum: String) {
    println("Commit: $hashNum")
    val path = "Git Internals\\task\\" +
            "test\\objects\\${hashNum.substring(0, 2)}\\${hashNum.substring(2, hashNum.length)}"
    val file = FileInputStream(path)
    val message = InflaterInputStream(file).readAllBytes().map { if (it == 0.toByte()) 10.toByte() else it}
        .toByteArray().decodeToString()
    val sec = message.substringAfterLast("> ").substringBeforeLast(" +")
    val timeZone = Regex("\\+\\d{4}").find(message.substringAfter("committer"))?.value
    var time = Instant.ofEpochSecond(sec.toLong()).atZone(ZoneOffset.of(timeZone))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
    val author = message.substringAfter("committer ").substringBefore(">")
        .replace("<", "") + " commit timestamp: "
    val commitMessage = message.substringAfter("\n\n")
    println(author + time + "\n$commitMessage")
    val nextCommit = message.substringAfter("parent ").substringBefore("\n")
    if (nextCommit.length == 40) getLogObject(nextCommit)
}
fun commitTree() {
    println("Enter commit-hash")
    val commit = readLine()!!.toString()
    getCommitObject(commit)
}
fun decodeFile(hash:String): String {
    val path = "Git Internals\\task\\" +
            "test\\objects\\${hash.substring(0, 2)}\\${hash.substring(2, hash.length)}"
    val file = FileInputStream(path)
    val message = InflaterInputStream(file).readAllBytes().map { if (it == 0.toByte()) 10.toByte() else it}
        .toByteArray().decodeToString()
    return message
}
fun getCommitObject(commit: String) {
    val tree = decodeFile(commit).substringAfter("tree ").substringBefore("\n")
    getTreeObject(tree)
}
fun getTreeObject(tree: String) {
    val path = "Git Internals\\task\\" +
            "test\\objects\\${tree.substring(0, 2)}\\${tree.substring(2, tree.length)}"
    val file = FileInputStream(path)
    val byteArray = InflaterInputStream(file).readAllBytes().map { if (it == 0.toByte()) 10.toByte() else it}
        .toByteArray()
    val (numbers, hash, fileNames) = getTreeParam(byteArray)
    val missMatch = emptyList<Int>().toMutableList()
    val regex = """[a-z]+\.[a-z]+"""
    for (i in fileNames) {
        if (!Regex(regex).containsMatchIn(i)) missMatch.add(fileNames.indexOf(i))
        else println(i)
    }
    for (i in missMatch) {
        val output = decodeFile(hash[i])
        val condition = Regex(regex).containsMatchIn(output)
        if (condition) println("""${fileNames[i]}/${Regex(regex).find(output)?.value}""")
    }


}
