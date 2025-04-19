package lib.mod

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ModIntTest {
    companion object {
        const val TEST_COUNT = 100

        private fun randomModInt() = Random.nextInt(1, ModInt.mod).m
    }

    @BeforeEach
    fun initMod() {
        ModInt.mod = 998_244_353
    }

    @RepeatedTest(TEST_COUNT)
    fun inv() {
        val a = randomModInt()
        assertEquals(1.m, a.inv() * a)
    }

    @RepeatedTest(TEST_COUNT)
    fun unaryPlus() {
        val a = randomModInt()
        assertEquals(a, +a)
    }

    @RepeatedTest(TEST_COUNT)
    fun unaryMinus() {
        val a = randomModInt()
        assertEquals(0.m, a + (-a))
    }

    @RepeatedTest(TEST_COUNT)
    fun plus() {
        val a = randomModInt()
        val b = randomModInt()
        assertEquals((a.x + b.x) % ModInt.mod, (a + b).x)
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
        assertEquals((a.x.toLong() * b.x).mod(ModInt.mod), (a * b).x)
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

        var naive = 1.m
        for (e in 0..100) {
            assertEquals(naive, a.pow(e))
            naive *= a
        }
    }

    @Test
    fun toStringTest() {
        val a = ModInt.new(1_234_567_890)
        assertEquals("236323537", a.toString())
    }

    @Test
    fun equalsTest() {
        val a = ModInt.raw(1)
        val b = ModInt.raw(1)
        assertEquals(a, b)
    }
}
