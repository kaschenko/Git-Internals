package gitinternals

import java.io.FileInputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.InflaterInputStream

fun commit(ArrayS: String, type: String): String {
    val seconds = Regex("\\d{9,20}").findAll(ArrayS)
    val timezone = Regex("\\+\\d{4}").findAll(ArrayS)
    var i = 0
    var secondsList = mutableListOf<String>()
    var timezoneList = mutableListOf<String>()
    for (j in seconds) {
        secondsList.add(j.value)
        i++
    }
    i = 0
    for (j in timezone) {
        timezoneList.add(j.value)
        i++
    }
    var time = Instant.ofEpochSecond(secondsList[0].toLong()).atZone(ZoneOffset.of(timezoneList[0]))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
    val regex = """\d{9,20}\s\+\d{4}""".toRegex()
    val timeSecond = Instant.ofEpochSecond(secondsList[1].toLong()).atZone(ZoneOffset.of(timezoneList[1]))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
    var alteredArrayS = "$type\n" + regex.replaceFirst(ArrayS, time).let {
        regex.replaceFirst(it, timeSecond)
    }.substringAfter("\n")
    Regex("""tree |parent |author |committer """).replace(alteredArrayS) {
            m -> m.value.replace(" ", ": ")
    }.also { alteredArrayS = it }
    alteredArrayS = Regex("<").replace(alteredArrayS, "")
    alteredArrayS = Regex(">").replaceFirst(alteredArrayS, " original timestamp:")
    alteredArrayS = Regex(">").replaceFirst(alteredArrayS, " commit timestamp:")
    alteredArrayS = Regex("\n\n").replace(alteredArrayS, "\ncommit message:\n")
    alteredArrayS = alteredArrayS.replace("parent", "parents")


    return alteredArrayS
}
fun tree(ArrayS: ByteArray) {
    val (numbers, hash, fileNames) = getTreeParam(ArrayS)
    println("*TREE*")
    for (i in 0 until hash.size) println("${numbers[i]} ${hash[i]} ${fileNames[i]}")

}
data class Result(val numbers: List<String>, val hash: MutableList<String>, val fileNames: List<String>)
fun getTreeParam(ArrayS: ByteArray): Result {
    val hash: MutableList<String> = emptyList<String>().toMutableList()
    for (i in 1..ArrayS.size - 1) {
        if (ArrayS[i] == 10.toByte() ) if ((i + 21) <= ArrayS.size) {
            hash += ArrayS.copyOfRange(i + 1, i + 21).joinToString("") { "%x".format(it) }
        }
    }
    val finAllText = Regex("""( [a-z]+.?-?[a-z]+)""")
        .findAll(ArrayS.decodeToString().substringAfter("\n"))
    var stringsOfText = ""
    for (i in finAllText) {
        stringsOfText += i.value
    }
    var stringOfDigits = ""
    val FindAllDigits = Regex("""\d+ """).findAll(ArrayS.decodeToString())
    for (i in FindAllDigits) {
        stringOfDigits += i.value
    }
    val fileNames = stringsOfText.split(" ").map { it.trim() }.toMutableList()
        .filter { it.isNotBlank() }
    val numbers = stringOfDigits.split(" ").map { it.trim() }.toMutableList()
        .filter { it.isNotBlank() }
    hash.removeAt(0)
    return Result(numbers, hash, fileNames)
}
fun getObject(input: String) {
    println("Enter git object hash:")
    val hashNum = readLine()!!.toString()
    val fileName = "Git Internals\\task\\" +
            "$input\\objects\\${hashNum.substring(0, 2)}\\${hashNum.substring(2, hashNum.length)}"
    val file = FileInputStream(fileName)
    val ArrayS = InflaterInputStream(file).readAllBytes().map { if (it == 0.toByte()) 10.toByte() else it}
        .toByteArray()
    val type = "*${ ArrayS.decodeToString().substringBefore(" ").uppercase() }*"
    if (ArrayS.decodeToString().substringBefore(" ").uppercase() == "COMMIT") {
        val commit = commit(ArrayS.decodeToString(), type)
        println(commit)
    } else if (ArrayS.decodeToString().substringBefore(" ").uppercase() == "TREE") {
        val tree = tree(ArrayS)
    } else println("$type\n" + ArrayS.decodeToString().substringAfter("\n"))
}