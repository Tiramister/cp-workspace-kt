package lib.convolution

import lib.mod.ModInt
import lib.mod.ModIntArray
import lib.mod.m
import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random
import kotlin.test.assertContentEquals

class ConvolutionTest {
    companion object {
        const val TEST_COUNT = 10
    }

    @RepeatedTest(TEST_COUNT)
    fun convolute() {
        val length = 1000

        fun randomModInt() = Random.nextInt(0, ModInt.mod).m

        val f = ModIntArray(length) { randomModInt() }
        val g = ModIntArray(length) { randomModInt() }

        val h = Convolution.convolute(f, g)

        val hNaive = ModIntArray(length * 2 - 1) { ModInt.raw(0) }
        f.forEachIndexed { fi, fx ->
            g.forEachIndexed { gi, gx ->
                hNaive[fi + gi] += fx * gx
            }
        }
        assertContentEquals(hNaive, h)
    }

    @RepeatedTest(TEST_COUNT)
    fun convoluteLong() {
        // val length = 1 shl 13
        val length = 5
        val xMax = 1L shl 25

        fun randomLong() = Random.nextLong(0, xMax)

        val f = LongArray(length) { randomLong() }
        val g = LongArray(length) { randomLong() }

        val h = Convolution.convoluteLong(f, g)

        val hNaive = LongArray(length * 2 - 1)
        f.forEachIndexed { fi, fx ->
            g.forEachIndexed { gi, gx ->
                hNaive[fi + gi] += fx * gx
            }
        }
        assertContentEquals(hNaive, h)
    }

    @RepeatedTest(TEST_COUNT)
    fun convoluteLongNegative() {
        val length = 1 shl 13
        val xMax = 1L shl 25

        fun randomLong() = Random.nextLong(-xMax, xMax + 1)

        val f = LongArray(length) { randomLong() }
        val g = LongArray(length) { randomLong() }

        val h = Convolution.convoluteLong(f, g)

        val hNaive = LongArray(length * 2 - 1)
        f.forEachIndexed { fi, fx ->
            g.forEachIndexed { gi, gx ->
                hNaive[fi + gi] += fx * gx
            }
        }
        assertContentEquals(hNaive, h)
    }

    @RepeatedTest(TEST_COUNT)
    fun convoluteLongSmall() {
        val length = 1 shl 10
        val xMax = 1L shl 25

        fun randomLong() = Random.nextLong(-xMax, xMax + 1)

        val f = LongArray(length) { randomLong() }
        val g = LongArray(length) { randomLong() }

        val h = Convolution.convoluteLongSmall(f, g)

        val hNaive = LongArray(length * 2 - 1)
        f.forEachIndexed { fi, fx ->
            g.forEachIndexed { gi, gx ->
                hNaive[fi + gi] += fx * gx
            }
        }
        assertContentEquals(hNaive, h)
    }
}
