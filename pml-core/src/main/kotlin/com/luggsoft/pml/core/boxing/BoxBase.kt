package com.luggsoft.pml.core.boxing

import kotlin.reflect.KCallable

/**
 * TODO
 *
 * @param TValue
 */
sealed class BoxBase<TValue : Any> : Box<TValue>
{
    /**
     * TODO
     *
     * @return
     */
    final override fun hashCode(): Int = this.value.hashCode()

    /**
     * TODO
     *
     * @return
     */
    final override fun toString(): String = this.value.toString()

    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean = when (this.value)
    {
        is Int -> this.value == other.toIntBox().value
        is Long -> this.value == other.toLongBox().value
        is Float -> this.value == other.toFloatBox().value
        is Double -> this.value == other.toDoubleBox().value
        is Boolean -> this.value == other.toBooleanBox().value
        is String -> this.value == other.toStringBox().value
        else -> false
    }

    /**
     * TODO
     *
     * @param name
     * @return
     */
    final override fun accessMember(name: String): Box<*>
    {
        val kCallable = this.value::class.members
            .firstOrNull { kCallable -> kCallable.name == name }
            ?: throw MemberAccessException(
                message = "No member named '$name' found for ${this.value::class}",
            )

        // @formatter:off
        try
        {
            return kCallable
                .call(this.value)
                .toBox()
        }
        catch (exception: Exception)
        {
            throw MemberAccessException(
                message = "Failed to access member '$name' for ${this.value::class}",
                cause = exception,
            )
        }
        // @formatter:on
    }

    /**
     * TODO
     *
     * @param name
     * @param argumentBoxes
     * @return
     */
    final override fun invokeMember(name: String, argumentBoxes: List<Box<*>>): Box<*>
    {
        val arguments = argumentBoxes.map(Box<*>::value).toTypedArray()

        val kCallable = this.value::class.members
            .firstOrNull { kCallable -> kCallable.name == name }
            ?: throw MemberInvocationException(
                message = "No member named '$name' found for ${this.value::class}",
            )

        // @formatter:off
        try
        {
            return kCallable
                .call(this.value, *arguments)
                .toBox()
        }
        catch (exception: Exception)
        {
            throw MemberInvocationException(
                message = "Failed to invoke member '$name' for ${this.value::class}",
                cause = exception,
            )
        }
        // @formatter:on
    }
}

/**
 * TODO
 *
 * @property value
 * @property clazz
 */
data class IntBox(
    override val value: Int,
    override val clazz: Class<Int> = Int::class.java,
) : BoxBase<Int>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = when (other)
    {
        is IntBox,
        is LongBox,
        is FloatBox,
        is DoubleBox,
        is BooleanBox,
        is StringBox -> this.value.compareTo(other.toIntBox().value)
        else -> Int.MIN_VALUE
    }
}

/**
 * TODO
 *
 * @property value
 * @property clazz
 */
data class LongBox(
    override val value: Long,
    override val clazz: Class<Long> = Long::class.java,
) : BoxBase<Long>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = when (other)
    {
        is IntBox,
        is LongBox,
        is FloatBox,
        is DoubleBox,
        is BooleanBox,
        is StringBox -> this.value.compareTo(other.toLongBox().value)
        else -> Int.MIN_VALUE
    }
}

/**
 * TODO
 *
 * @property value
 * @property clazz
 */
data class FloatBox(
    override val value: Float,
    override val clazz: Class<Float> = Float::class.java,
) : BoxBase<Float>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = when (other)
    {
        is IntBox,
        is LongBox,
        is FloatBox,
        is DoubleBox,
        is BooleanBox,
        is StringBox -> this.value.compareTo(other.toFloatBox().value)
        else -> Int.MIN_VALUE
    }
}

/**
 * TODO
 *
 * @property value
 * @property clazz
 */
data class DoubleBox(
    override val value: Double,
    override val clazz: Class<Double> = Double::class.java,
) : BoxBase<Double>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = when (other)
    {
        is IntBox,
        is LongBox,
        is FloatBox,
        is DoubleBox,
        is BooleanBox,
        is StringBox -> this.value.compareTo(other.toDoubleBox().value)
        else -> Int.MIN_VALUE
    }
}

/**
 * TODO
 *
 * @property value
 * @property clazz
 */
open class BooleanBox(
    override val value: Boolean,
    override val clazz: Class<Boolean> = Boolean::class.java,
) : BoxBase<Boolean>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = when (other)
    {
        is IntBox,
        is LongBox,
        is FloatBox,
        is DoubleBox,
        is BooleanBox,
        is StringBox -> this.value.compareTo(other.toBooleanBox().value)
        else -> Int.MIN_VALUE
    }

    /**
     *
     */
    object True : BooleanBox(
        value = true,
    )

    /**
     *
     */
    object False : BooleanBox(
        value = false,
    )
}

/**
 * TODO
 *
 * @property value
 * @property clazz
 */
data class StringBox(
    override val value: String,
    override val clazz: Class<String> = String::class.java,
) : BoxBase<String>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = when (other)
    {
        is IntBox,
        is LongBox,
        is FloatBox,
        is DoubleBox,
        is BooleanBox,
        is StringBox -> this.value.compareTo(other.toStringBox().value)
        else -> Int.MIN_VALUE
    }
}

/**
 * TODO
 *
 * @param TValue
 * @property value
 * @property clazz
 */
data class ObjectBox<TValue : Any>(
    override val value: TValue,
    override val clazz: Class<out TValue> = value::class.java
) : BoxBase<TValue>()
{
    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = TODO()
}

/**
 *
 */
object NullBox : BoxBase<Unit>()
{
    /**
     *
     */
    override val value: Unit = Unit

    /**
     *
     */
    override val clazz: Class<Unit> = Unit::class.java

    /**
     * TODO
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Box<*>): Int = Int.MIN_VALUE
}
