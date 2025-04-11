package PACKAGE

@OptIn(ExperimentalStdlibApi::class)
fun main() {
}

/* ==================== Libraries ==================== */
object Sc {
    private val buffer: ArrayDeque<String> = ArrayDeque()
    fun string(): String {
        while (buffer.isEmpty()) buffer.addAll(readln().split(" "))
        return buffer.removeFirst()
    }

    fun int(): Int = string().toInt()
    fun long(): Long = string().toLong()
}
