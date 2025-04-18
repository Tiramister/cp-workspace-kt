package lib.convolution

import lib.mod.ModInt
import lib.mod.ModIntArray
import kotlin.math.min

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
            167772161 to 3, // 25
            469762049 to 3, // 26
            754974721 to 11, // 24
            998244353 to 3, // 23
            1107296257 to 10, // 25
            1811939329 to 13, // 26
            2013265921 to 31, // 27
            2113929217 to 5, // 25
            2130706433 to 3, // 24
        )

    /**
     * [getZetas] のキャッシュ
     */
    private val zetaCaches: MutableMap<Int, IntArray> = mutableMapOf()

    /**
     * 現在の mod における、1 の 2^k 乗根のテーブルを求める
     *
     * @return k 番目 (0-indexed) の要素が 1 の 2^k 乗根であるリスト
     */
    private fun getZetas(): ModIntArray {
        val zetas =
            zetaCaches[ModInt.mod] ?: run {
                val root =
                    primitiveRoot.getOrElse(ModInt.mod) {
                        throw IllegalArgumentException("${ModInt.mod} の原子根は用意されていません")
                    }

                val size = (ModInt.mod - 1).countTrailingZeroBits() + 1
                val zetas =
                    IntArray(size) { k ->
                        ModInt.raw(root).pow((ModInt.mod - 1) shr k).x
                    }

                zetaCaches[ModInt.mod] = zetas
                zetas
            }
        return ModIntArray(zetas.size) { ModInt.raw(zetas[it]) }
    }

    /**
     * 数論変換 (F_mod 上のフーリエ変換)
     *
     * ただし並び替えは行わない
     *
     * @param f 長さが 2 冪である数列
     */
    private fun ModIntArray.ntt() {
        val zetas = getZetas()

        val logN = ceilLog2(size)

        for (k in logN - 1 downTo 0) {
            val m = 1 shl k
            val zeta = zetas[k + 1]
            var zetaPow = ModInt.raw(1)

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
    private fun ModIntArray.inverseNtt() {
        val zetas = getZetas()

        val logN = ceilLog2(size)

        for (k in 0..<logN) {
            val m = 1 shl k
            val zetaInv = zetas[k + 1].inv()
            var zetaInvPow = ModInt.raw(1)

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

        val nInv = ModInt.new(size).inv()
        for (i in indices) this[i] *= nInv
    }

    /**
     * F_mod 上の畳み込み
     *
     * @throws IllegalArgumentException mod が [primitiveRoot] にない場合
     */
    fun convolute(
        f: ModIntArray,
        g: ModIntArray,
    ): ModIntArray {
        val fSize = f.size
        val gSize = g.size

        if (fSize == 0 || gSize == 0) return f.copyOf(0)

        return if (min(fSize, gSize) <= 60) {
            // 小さいときは愚直にやる
            val h = ModIntArray(fSize + gSize - 1) { ModInt.raw(0) }
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

            val h = ModIntArray(n) { fTmp[it] * gTmp[it] }
            h.inverseNtt()
            return h.copyOf(fSize + gSize - 1)
        }
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
        // 後で mod を復元する用
        val originalMod = ModInt.mod

        val mods = arrayOf(2013265921, 2113929217, 2130706433)
        val hs =
            mods.map { mod ->
                ModInt.mod = mod
                convolute(
                    ModIntArray(f.size) { ModInt.new(f[it]) },
                    ModIntArray(g.size) { ModInt.new(g[it]) },
                )
            }

        ModInt.mod = mods[1]
        val m0InvMod1 = ModInt.raw(mods[0]).inv()

        ModInt.mod = mods[2]
        val m0m1InvMod2 =
            ModInt
                .new(mods[0].toLong() * mods[1])
                .inv()
                .x
                .toLong()

        val result =
            LongArray(hs[0].size) { i ->
                // 正の数になるようにずらす
                val rems =
                    mods.mapIndexed { j, mod ->
                        ModInt.mod = mod
                        hs[j][i] - Long.MIN_VALUE
                    }

                ModInt.mod = mods[1]
                val v0 = ((rems[1] - rems[0].x) * m0InvMod1).x.toLong()

                ModInt.mod = mods[2]
                val v1 = ((rems[2] - rems[0].x - v0 * mods[0].toLong()) * m0m1InvMod2).x.toLong()

                // MOD 2^64
                val x =
                    rems[0].x.toULong() +
                        (v0.toULong() * mods[0].toUInt()) +
                        (v1.toULong() * mods[0].toUInt() * mods[1].toUInt())

                // ずらした分を戻す
                x.toLong() + Long.MIN_VALUE
            }

        // mod を復元する
        ModInt.mod = originalMod
        return result
    }

    // (2113929217 * 2130706433 - 1) / 2 = 2252081290784276480
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
        // 後で mod を復元する用
        val originalMod = ModInt.mod

        val mods = arrayOf(2113929217, 2130706433)
        val hs =
            mods.map { mod ->
                ModInt.mod = mod
                convolute(
                    ModIntArray(f.size) { ModInt.new(f[it]) },
                    ModIntArray(g.size) { ModInt.new(g[it]) },
                )
            }

        ModInt.mod = mods[1]
        val m0InvMod1 = ModInt.raw(mods[0]).inv()

        val result =
            LongArray(hs[0].size) { i ->
                // 正の数になるようにずらす
                val rems =
                    mods.mapIndexed { j, mod ->
                        ModInt.mod = mod
                        hs[j][i] + SMALL_CONV_MAX
                    }

                ModInt.mod = mods[1]
                val v0 = ((rems[1] - rems[0].x) * m0InvMod1).x.toLong()

                // MOD 2^64
                val x = rems[0].x.toULong() + v0.toULong() * mods[0].toULong()

                // ずらした分を戻す
                x.toLong() - SMALL_CONV_MAX
            }

        // mod を復元する
        ModInt.mod = originalMod
        return result
    }
}
