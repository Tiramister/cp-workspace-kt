package lib.mod

import lib.mod.Mod.Companion.new
import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.test.assertEquals

class ModIntTest {
    companion object {
        const val TEST_COUNT = 100

        val UInt.m: ModInt<Mod998244353>
            get() = Mod998244353.new(this)

        private fun randomModInt() = Random.nextUInt(1U, Mod998244353.mod).m
    }

    @RepeatedTest(TEST_COUNT)
    fun inv() {
        val a = randomModInt()
        assertEquals(1U.m, a.inv() * a)
    }

    @RepeatedTest(TEST_COUNT)
    fun unaryPlus() {
        val a = randomModInt()
        assertEquals(a, +a)
    }

    @RepeatedTest(TEST_COUNT)
    fun unaryMinus() {
        val a = randomModInt()
        assertEquals(0U.m, a + (-a))
    }

    @RepeatedTest(TEST_COUNT)
    fun plus() {
        val a = randomModInt()
        val b = randomModInt()
        assertEquals((a.x + b.x) % Mod998244353.mod, (a + b).x)
    }

    @RepeatedTest(TEST_COUNT)
    fun minus() {
        val a = randomModInt()
        val b = randomModInt()
        assertEquals(a + (-b), a - b)
    }

    @RepeatedTest(TEST_COUNT)
    fun times() {
        val a = randomModInt()
        val b = randomModInt()
        assertEquals((a.x.toULong() * b.x).mod(Mod998244353.mod), (a * b).x)
    }

    @RepeatedTest(TEST_COUNT)
    fun div() {
        val a = randomModInt()
        val b = randomModInt()
        assertEquals(a * b.inv(), a / b)
    }

    @RepeatedTest(TEST_COUNT)
    fun pow() {
        val a = randomModInt()

        var naive = 1U.m
        for (e in 0..100) {
            assertEquals(naive, a.pow(e))
            naive *= a
        }
    }
}
