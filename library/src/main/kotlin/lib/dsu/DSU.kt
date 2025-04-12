package lib.dsu

/**
 * 集合のマージを高速に行うデータ構造
 */
class DSU(
    private val size: Int,
) {
    /**
     * 非負 -> 親の要素
     * 負 -> その要素が代表元である集合のサイズ (符号反転)
     */
    private val parentOrSizes = IntArray(size) { -1 }

    /**
     * v が属する集合の代表元を返す
     */
    fun leader(v: Int): Int =
        if (parentOrSizes[v] < 0) {
            v
        } else {
            parentOrSizes[v] = leader(parentOrSizes[v])
            parentOrSizes[v]
        }

    /**
     * u が属する集合と v が属する集合を合併し、新たな集合の代表元を返す
     */
    fun merge(
        u: Int,
        v: Int,
    ): Int {
        var u = leader(u)
        var v = leader(v)

        if (u != v) {
            if (-parentOrSizes[u] < -parentOrSizes[v]) u = v.also { v = u } // swap
            parentOrSizes[u] += parentOrSizes[v]
            parentOrSizes[v] = u
        }
        return u
    }

    /**
     * u と v が同じ集合に属するかを返す
     */
    fun same(
        u: Int,
        v: Int,
    ): Boolean = leader(u) == leader(v)

    /**
     * v が属する集合の大きさを返す
     */
    fun sizeOf(v: Int): Int = -parentOrSizes[leader(v)]

    /** 各集合を返す */
    @OptIn(ExperimentalStdlibApi::class)
    fun groups(): List<List<Int>> {
        val groups = List(size) { mutableListOf<Int>() }
        for (v in 0..<size) groups[leader(v)].add(v)
        return groups.mapNotNull { group -> if (group.isEmpty()) null else group.toList() }
    }
}
