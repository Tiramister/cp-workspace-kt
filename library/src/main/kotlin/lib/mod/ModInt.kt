package lib.mod

import lib.mod.Mod.Companion.new
import lib.mod.Mod.Companion.raw
import java.util.Objects

/**
 * 除数を保持するインターフェース
 *
 * これを継承した object を `ModInt` に渡すことを想定している。
 *
 * 使用例
 * ```
 * object Mod998244353 : Mod() {
 *     override val mod = 998244353U
 * }
 * val x = ModInt.new(1234567890U, Mod998244353)
 * val y = Mod998244353.modInt(1234567890U)
 * ```
 */
abstract class Mod {
    /** 除数 */
    abstract val mod: UInt

    companion object {
        /**
         * x が mod より小さいことを仮定した、より高速なコンストラクタ
         *
         * @param x mod より小さくなければならない
         */
        fun <T : Mod> T.raw(x: UInt): ModInt<T> = ModInt.raw(x, this)

        fun <T : Mod> T.new(x: UInt): ModInt<T> = ModInt.new(x, this)

        fun <T : Mod> T.new(x: Int): ModInt<T> = ModInt.new(x, this)

        fun <T : Mod> T.new(x: Long): ModInt<T> = ModInt.new(x, this)

        fun <T : Mod> T.new(x: ULong): ModInt<T> = ModInt.new(x, this)

        fun <T : Mod> T.array(size: Int): ModIntArray<T> = ModIntArray(size, this)

        fun <T : Mod> T.array(
            size: Int,
            init: (Int) -> ModInt<T>,
        ): ModIntArray<T> = ModIntArray(size, init, this)
    }
}

object Mod998244353 : Mod() {
    /** 除数 (998244353) */
    override val mod = 998244353U
}

/**
 * 自動で剰余を取る整数クラス
 *
 * 使用例
 * ```
 * object Mod998244353 : Mod() {
 *     override val mod = 998244353U
 * }
 * val x = ModInt.new(1234567890U, Mod998244353)
 * val y = Mod998244353.new(1234567890U)
 * ```
 *
 * @param x 値
 * @param modObject 除数を保持するオブジェクト
 */
class ModInt<T : Mod> private constructor(
    val x: UInt,
    val modObject: T,
) {
    companion object {
        /**
         * x が mod より小さいことを仮定した、より高速なコンストラクタ
         *
         * @param x mod より小さくなければならない
         * @param modObject 除数を保持するオブジェクト
         */
        fun <T : Mod> raw(
            x: UInt,
            modObject: T,
        ) = ModInt(x, modObject)

        fun <T : Mod> new(
            x: UInt,
            modObject: T,
        ) = raw(if (x >= modObject.mod) x.mod(modObject.mod) else x, modObject)

        fun <T : Mod> new(
            x: Int,
            modObject: T,
        ) = new(x.toLong().mod(modObject.mod.toLong()).toUInt(), modObject)

        fun <T : Mod> new(
            x: ULong,
            modObject: T,
        ) = new(x.mod(modObject.mod), modObject)

        fun <T : Mod> new(
            x: Long,
            modObject: T,
        ) = new(x.mod(modObject.mod.toLong()).toUInt(), modObject)
    }

    /**
     * 逆元
     *
     * mod は素数でなくてもいいが、値は mod と互いに素である必要がある
     */
    fun inv(): ModInt<T> {
        // 拡張ユークリッドの互除法
        var s = x.toLong()
        var t = modObject.mod.toLong()
        var xs = 1L
        var xt = 0L

        while (t != 0L) {
            val div = s / t
            s = t.also { t = s - t * div }
            xs = xt.also { xt = xs - xt * div }
        }

        return modObject.new(xs)
    }

    operator fun unaryPlus() = this

    operator fun unaryMinus() = modObject.raw(if (x == 0U) 0U else modObject.mod - x)

    operator fun plus(other: ModInt<T>) =
        run {
            val sum = x + other.x
            modObject.raw(if (sum < modObject.mod) sum else sum - modObject.mod)
        }

    operator fun minus(other: ModInt<T>) = modObject.raw(if (x >= other.x) x - other.x else x + modObject.mod - other.x)

    operator fun times(other: ModInt<T>) = modObject.new(x.toULong() * other.x)

    operator fun div(other: ModInt<T>) = this * other.inv()

    /** 累乗 */
    fun pow(n: ULong): ModInt<T> {
        var result = modObject.raw(1U)
        var base = modObject.raw(this.x) // 更新しないように複製

        var n = n
        while (n > 0UL) {
            if (n and 1UL != 0UL) result *= base
            n = n shr 1
            base *= base
        }
        return result
    }

    /** 累乗 */
    fun pow(n: UInt): ModInt<T> = pow(n.toULong())

    /** 負の数にも対応した累乗 */
    fun pow(n: Long): ModInt<T> = if (n >= 0L) pow(n.toULong()) else pow((-n).toULong()).inv()

    /** 負の数にも対応した累乗 */
    fun pow(n: Int): ModInt<T> = pow(n.toLong())

    // ---------- その他 ----------
    override fun toString() = x.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModInt<*>) return false
        return x == other.x && modObject == other.modObject
    }

    override fun hashCode(): Int = Objects.hash(x, modObject)

    // ModInt + Int
    operator fun plus(other: Int) = this + modObject.new(other)

    operator fun minus(other: Int) = this - modObject.new(other)

    operator fun times(other: Int) = this * modObject.new(other)

    operator fun div(other: Int) = this / modObject.new(other)

    // ModInt + UInt
    operator fun plus(other: UInt) = this + modObject.new(other)

    operator fun minus(other: UInt) = this - modObject.new(other)

    operator fun times(other: UInt) = this * modObject.new(other)

    operator fun div(other: UInt) = this / modObject.new(other)

    // ModInt + Long
    operator fun plus(other: Long) = this + modObject.new(other)

    operator fun minus(other: Long) = this - modObject.new(other)

    operator fun times(other: Long) = this * modObject.new(other)

    operator fun div(other: Long) = this / modObject.new(other)

    // ModInt + ULong
    operator fun plus(other: ULong) = this + modObject.new(other)

    operator fun minus(other: ULong) = this - modObject.new(other)

    operator fun times(other: ULong) = this * modObject.new(other)

    operator fun div(other: ULong) = this / modObject.new(other)

    // Int + ModInt
    operator fun Int.plus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) + other

    operator fun Int.minus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) - other

    operator fun Int.times(other: ModInt<T>): ModInt<T> = new(this, other.modObject) * other

    operator fun Int.div(other: ModInt<T>): ModInt<T> = new(this, other.modObject) / other

    // UInt + ModInt
    operator fun UInt.plus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) + other

    operator fun UInt.minus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) - other

    operator fun UInt.times(other: ModInt<T>): ModInt<T> = new(this, other.modObject) * other

    operator fun UInt.div(other: ModInt<T>): ModInt<T> = new(this, other.modObject) / other

    // Long + ModInt
    operator fun Long.plus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) + other

    operator fun Long.minus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) - other

    operator fun Long.times(other: ModInt<T>): ModInt<T> = new(this, other.modObject) * other

    operator fun Long.div(other: ModInt<T>): ModInt<T> = new(this, other.modObject) / other

    // ULong + ModInt
    operator fun ULong.plus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) + other

    operator fun ULong.minus(other: ModInt<T>): ModInt<T> = new(this, other.modObject) - other

    operator fun ULong.times(other: ModInt<T>): ModInt<T> = new(this, other.modObject) * other

    operator fun ULong.div(other: ModInt<T>): ModInt<T> = new(this, other.modObject) / other
}
