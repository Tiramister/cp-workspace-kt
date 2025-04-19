package lib.mod

val Int.m: ModInt
    get() = ModInt.new(this)
val Long.m: ModInt
    get() = ModInt.new(this)

/**
 * 自動で剰余を取る整数クラス
 *
 * デフォルトの法は 998244353 で、`ModInt.mod` に代入することで変えられる
 *
 * 初期化の例
 *
 * ```
 * val x = ModInt.new(1_000_000_000)
 * val y = ModInt.raw(1) // 値が mod より小さい場合は raw の方が速い
 * val z = 1.m // new が呼ばれる
 * ```
 *
 * @param x 値
 */
@JvmInline
value class ModInt private constructor(
    val x: Int,
) {
    companion object {
        var mod: Int = 998_244_353

        /**
         * x が mod より小さいことを仮定した、より高速なコンストラクタ
         *
         * @param x mod より小さい非負整数
         */
        fun raw(x: Int) = ModInt(x)

        fun raw(x: UInt) = ModInt(x.toInt())

        fun new(x: Int) = raw(x.mod(mod))

        fun new(x: Long) = raw(x.mod(mod))

        fun new(x: UInt) = raw(x.mod(mod.toUInt()).toInt())

        fun new(x: ULong) = raw(x.mod(mod.toUInt()).toInt())
    }

    /**
     * 逆元
     *
     * mod は素数でなくてもいいが、値は mod と互いに素である必要がある
     */
    fun inv(): ModInt {
        // 拡張ユークリッドの互除法
        var s = x.toLong()
        var t = mod.toLong()
        var xs = 1L
        var xt = 0L

        while (t != 0L) {
            val div = s / t
            s = t.also { t = s - t * div }
            xs = xt.also { xt = xs - xt * div }
        }

        return new(xs)
    }

    operator fun unaryPlus() = this

    operator fun unaryMinus() = raw(if (x == 0) 0 else mod - x)

    operator fun plus(other: ModInt) =
        run {
            val sum = x.toUInt() + other.x.toUInt()
            raw(if (sum < mod.toUInt()) sum else sum - mod.toUInt())
        }

    operator fun minus(other: ModInt) = raw(if (x >= other.x) x - other.x else x + mod - other.x)

    operator fun times(other: ModInt) = new(x.toLong() * other.x)

    operator fun div(other: ModInt) = this * other.inv()

    /** 累乗 */
    fun pow(n: ULong): ModInt {
        var result = raw(1)
        var base = raw(this.x) // 更新しないように複製

        var n = n
        while (n > 0UL) {
            if (n and 1UL != 0UL) result *= base
            n = n shr 1
            base *= base
        }
        return result
    }

    /** 累乗 */
    fun pow(n: UInt): ModInt = pow(n.toULong())

    /** 負の数にも対応した累乗 */
    fun pow(n: Long): ModInt = if (n >= 0L) pow(n.toULong()) else pow((-n).toULong()).inv()

    /** 負の数にも対応した累乗 */
    fun pow(n: Int): ModInt = pow(n.toLong())

    // ---------- その他 ----------
    override fun toString() = x.toString()

    // ModInt + Int
    operator fun plus(other: Int) = this + new(other)

    operator fun minus(other: Int) = this - new(other)

    operator fun times(other: Int) = this * new(other)

    operator fun div(other: Int) = this / new(other)

    // ModInt + Long
    operator fun plus(other: Long) = this + new(other)

    operator fun minus(other: Long) = this - new(other)

    operator fun times(other: Long) = this * new(other)

    operator fun div(other: Long) = this / new(other)

    // Int + ModInt
    operator fun Int.plus(other: ModInt): ModInt = new(this) + other

    operator fun Int.minus(other: ModInt): ModInt = new(this) - other

    operator fun Int.times(other: ModInt): ModInt = new(this) * other

    operator fun Int.div(other: ModInt): ModInt = new(this) / other

    // Long + ModInt
    operator fun Long.plus(other: ModInt): ModInt = new(this) + other

    operator fun Long.minus(other: ModInt): ModInt = new(this) - other

    operator fun Long.times(other: ModInt): ModInt = new(this) * other

    operator fun Long.div(other: ModInt): ModInt = new(this) / other
}

/**
 * [ModInt] の配列
 *
 * `List<ModInt>` ではパフォーマンスが悪いため、内部的に `IntArray` として持つことで高速化する
 */
@JvmInline
value class ModIntArray private constructor(
    /**
     * 全ての要素は既に mod が取られていると仮定する
     */
    private val array: IntArray,
) : Collection<ModInt> {
    constructor(size: Int) : this(IntArray(size))

    constructor(size: Int, init: (Int) -> ModInt) : this(IntArray(size) { init(it).x })

    operator fun get(index: Int) = ModInt.raw(array[index])

    operator fun set(
        index: Int,
        value: ModInt,
    ) {
        array[index] = value.x
    }

    override val size: Int
        get() = array.size

    override fun isEmpty() = array.isEmpty()

    override fun iterator(): Iterator<ModInt> =
        object : Iterator<ModInt> {
            private var index = 0

            override fun hasNext(): Boolean = index < array.size

            override fun next(): ModInt =
                if (index < array.size) {
                    ModInt.raw(array[index++])
                } else {
                    throw NoSuchElementException(index.toString())
                }
        }

    override fun containsAll(elements: Collection<ModInt>) = elements.all { contains(it) }

    override fun contains(element: ModInt) = array.contains(element.x)

    fun copyOf() = ModIntArray(array.copyOf(size))

    fun copyOf(size: Int) = ModIntArray(array.copyOf(size))
}
