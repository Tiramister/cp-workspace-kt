package lib.mod

import lib.mod.Mod.Companion.new
import java.util.Objects
import kotlin.NoSuchElementException

/**
 * [ModInt] の配列
 *
 * `List<ModInt<T>>` ではパフォーマンスが悪すぎるため、内部的に `IntArray` として持つことで高速化する
 */
class ModIntArray<T : Mod>(
    private val array: IntArray,
    val modObject: T,
) : Collection<ModInt<T>> {
    constructor(size: Int, modObject: T) : this(IntArray(size), modObject)

    constructor(size: Int, init: (Int) -> ModInt<T>, modObject: T) : this(IntArray(size) { init(it).x.toInt() }, modObject)

    operator fun get(index: Int) = modObject.new(array[index])

    operator fun set(
        index: Int,
        value: ModInt<T>,
    ) {
        array[index] = value.x.toInt()
    }

    override val size: Int
        get() = array.size

    override fun isEmpty() = array.isEmpty()

    override fun iterator(): Iterator<ModInt<T>> =
        object : Iterator<ModInt<T>> {
            private var index = 0

            override fun hasNext(): Boolean = index < array.size

            override fun next(): ModInt<T> =
                if (index < array.size) {
                    modObject.new(array[index++])
                } else {
                    throw NoSuchElementException(index.toString())
                }
        }

    override fun containsAll(elements: Collection<ModInt<T>>) = elements.all { contains(it) }

    override fun contains(element: ModInt<T>) = array.contains(element.x.toInt())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModIntArray<*>) return false
        return this.array.contentEquals(other.array) && modObject == other.modObject
    }

    fun copyOf() = ModIntArray(array.copyOf(size), modObject)

    fun copyOf(size: Int) = ModIntArray(array.copyOf(size), modObject)

    override fun hashCode(): Int = Objects.hash(array, modObject)
}
