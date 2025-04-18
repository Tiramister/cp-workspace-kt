package lib.convolution

import lib.mod.Mod
import lib.mod.Mod.Companion.array
import lib.mod.Mod.Companion.new
import lib.mod.Mod.Companion.raw
import lib.mod.ModInt
import lib.mod.ModIntArray
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * 畳み込みを行う関数を集めたオブジェクト
 */
@OptIn(ExperimentalStdlibApi::class)
object Convolution {
    /**
     * log_2(n) を切り上げた値を返す
     */
    private fun ceilLog2(n: Int): Int {
        val floorLog2 = n.takeHighestOneBit().countTrailingZeroBits()
        return if (n.countOneBits() == 1) floorLog2 else floorLog2 + 1
    }

    /**
     * 畳み込みでよく使う MOD の原子根テーブル
     *
     * ここにない素数での畳み込みはできない
     */
    private val primitiveRoot =
        mapOf(
            167772161U to 3U, // 25
            469762049U to 3U, // 26
            754974721U to 11U, // 24
            998244353U to 3U, // 23
            1107296257U to 10U, // 25
            1811939329U to 13U, // 26
            2013265921U to 31U, // 27
            2113929217U to 5U, // 25
            2130706433U to 3U, // 24
        )

    /**
     * [getZetas] のキャッシュ
     */
    private val zetaCaches: MutableMap<KClass<out Mod>, ModIntArray<*>> = mutableMapOf()

    /**
     * 1 の 2^k 乗根のテーブルを求める
     *
     * @return k 番目 (0-indexed) の要素が 1 の 2^k 乗根であるリスト
     */
    private fun <T : Mod> getZetas(modObject: T): ModIntArray<T> {
        val mod = modObject.mod
        val modClass = modObject::class

        return zetaCaches[modClass] as? ModIntArray<T> ?: run {
            val root =
                primitiveRoot.getOrElse(mod) {
                    throw IllegalArgumentException("$mod の原子根は用意されていません")
                }

            val size = (mod - 1U).countTrailingZeroBits() + 1

            val zetas =
                modObject.array(size) { k ->
                    ModInt.raw(root, modObject).pow((mod - 1U) shr k)
                }
            zetaCaches[modClass] = zetas
            zetas
        }
    }

    /**
     * 数論変換 (F_mod 上のフーリエ変換)
     *
     * ただし並び替えは行わない
     *
     * @param f 長さが 2 冪である数列
     */
    private fun <T : Mod> ModIntArray<T>.ntt() {
        val zetas = getZetas(modObject)

        val logN = ceilLog2(size)

        for (k in logN - 1 downTo 0) {
            val m = 1 shl k
            val zeta = zetas[k + 1]
            var zetaPow = modObject.raw(1U)

            // 長さ 2m のブロックに分割し、さらに各ブロックを半分にして重ね合わせるイメージ
            for (p in 0..<m) {
                for (s in indices step (m * 2)) {
                    val l = this[s + p]
                    val r = this[s + p + m]

                    this[s + p] = l + r
                    this[s + p + m] = (l - r) * zetaPow
                }
                zetaPow *= zeta
            }
        }
    }

    /**
     * [ntt] の逆変換
     *
     * @param f 長さが 2 冪である数列
     */
    private fun <T : Mod> ModIntArray<T>.inverseNtt() {
        val zetas = getZetas(modObject)

        val logN = ceilLog2(size)

        for (k in 0..<logN) {
            val m = 1 shl k
            val zetaInv = zetas[k + 1].inv()
            var zetaInvPow = ModInt.raw(1U, modObject)

            // 長さ 2m のブロックに分割し、さらに各ブロックを半分にして重ね合わせるイメージ
            for (p in 0..<m) {
                for (s in indices step (m * 2)) {
                    val l = this[s + p]
                    val r = this[s + p + m] * zetaInvPow

                    this[s + p] = l + r
                    this[s + p + m] = l - r
                }
                zetaInvPow *= zetaInv
            }
        }

        val nInv = modObject.new(size).inv()
        for (i in indices) this[i] *= nInv
    }

    /**
     * F_mod 上の畳み込み
     *
     * @throws IllegalArgumentException mod が [primitiveRoot] にない場合
     */
    fun <T : Mod> convolute(
        f: ModIntArray<T>,
        g: ModIntArray<T>,
    ): ModIntArray<T> {
        val fSize = f.size
        val gSize = g.size

        if (fSize == 0 || gSize == 0) return f.copyOf(0)

        val modObject = f.modObject

        return if (min(fSize, gSize) <= 60) {
            // 小さいときは愚直にやる
            val h = modObject.array(fSize + gSize - 1) { modObject.raw(0U) }
            f.forEachIndexed { fi, fx ->
                g.forEachIndexed { gi, gx ->
                    h[fi + gi] += fx * gx
                }
            }
            h
        } else {
            val n = 1 shl ceilLog2(fSize + gSize - 1)
            val fTmp = f.copyOf(n)
            val gTmp = g.copyOf(n)
            fTmp.ntt()
            gTmp.ntt()

            val h = modObject.array(n) { fTmp[it] * gTmp[it] }
            h.inverseNtt()
            return h.copyOf(fSize + gSize - 1)
        }
    }

    private object Mod2013265921 : Mod() {
        override val mod = 2013265921U
    }

    private object Mod2113929217 : Mod() {
        override val mod = 2113929217U
    }

    private object Mod2130706433 : Mod() {
        override val mod = 2130706433U
    }

    /**
     * 剰余を取らない、普通の畳み込み
     *
     * - 畳み込み結果の全ての値が Long に収まることが条件
     * - 負の数にも対応している
     */
    fun convoluteLong(
        f: LongArray,
        g: LongArray,
    ): LongArray {
        val mods = arrayOf(Mod2013265921, Mod2113929217, Mod2130706433)
        val hs =
            mods.map { mod ->
                convolute(
                    mod.array(f.size) { i -> mod.new(f[i]) },
                    mod.array(g.size) { i -> mod.new(g[i]) },
                )
            }

        val m0InvMod1 = ModInt.raw(mods[0].mod, mods[1]).inv()
        val m0m1InvMod2 =
            ModInt
                .new(mods[0].mod.toULong() * mods[1].mod, mods[2])
                .inv()
                .x
                .toLong()

        return LongArray(hs[0].size) { i ->
            // 正の数になるようにずらす
            val rems = (0..2).map { j -> hs[j][i] - Long.MIN_VALUE }

            // MOD mod1
            val v0 = ((rems[1] - rems[0].x) * m0InvMod1).x.toLong()
            // MOD mod2
            val v1 = ((rems[2] - rems[0].x - v0 * mods[0].mod.toLong()) * m0m1InvMod2).x.toLong()
            // MOD 2^64
            val x =
                rems[0].x.toULong() +
                    (v0.toULong() * mods[0].mod) +
                    (v1.toULong() * mods[0].mod * mods[1].mod)

            // ずらした分を戻す
            x.toLong() + Long.MIN_VALUE
        }
    }

    private const val SMALL_CONV_MAX = 2252081290784276480L

    /**
     * 結果が極端に大きくない場合に使える、[convoluteLong] より高速な畳み込み
     *
     * - 畳み込み結果の全ての値の絶対値が 2,252,081,290,784,276,480 以下であることが条件
     * - 計算量は [convoluteLong] の約 2/3
     * - 負の数にも対応している
     */
    fun convoluteLongSmall(
        f: LongArray,
        g: LongArray,
    ): LongArray {
        // (2113929217 * 2130706433 - 1) / 2 = 2252081290784276480
        val mods = arrayOf(Mod2113929217, Mod2130706433)
        val hs =
            mods.map { mod ->
                convolute(
                    mod.array(f.size) { i -> mod.new(f[i]) },
                    mod.array(g.size) { i -> mod.new(g[i]) },
                )
            }

        val m0InvMod1 = ModInt.raw(mods[0].mod, mods[1]).inv()

        return LongArray(hs[0].size) { i ->
            // 正の数になるようにずらす
            val rems = (0..1).map { j -> hs[j][i] + SMALL_CONV_MAX }

            // MOD mod1
            val v0 = ((rems[1] - rems[0].x) * m0InvMod1).x.toLong()
            // MOD 2^64
            val x = rems[0].x.toULong() + v0.toULong() * mods[0].mod

            // ずらした分を戻す
            x.toLong() - SMALL_CONV_MAX
        }
    }
}
