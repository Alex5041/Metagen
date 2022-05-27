package properties.primitive

import lexer.PositionalException
import properties.*
import properties.Function
import token.Token

/**
 * Stores Array, String, Int, Double values
 */
abstract class Primitive(protected open var value: Any, parent: Type?) : Property(parent) {
    open fun getIndex() = 0
    open fun getPValue() = value
    override fun toString() = "$value"

    fun getSymbol(): String {
        return when (value) {
            is Number -> "(NUMBER)"
            is String -> "(STRING)"
            is MutableList<*> -> "[]"
            else -> throw Exception("unsupported type")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Primitive) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun getPropertyOrNull(name: String) = when (name) {
        "this" -> this
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[getIndex()][name]?.let { it(this) }
            ?: (if (getIndex() in 2..3)
                properties[1][name]?.let { it(this) } ?: properties[0][name]?.let { it(this) }
            else properties[0][name]?.let { it(this) })
    }

    override fun getProperty(token: Token): Property = when (token.value) {
        "this" -> this
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[getIndex()][token.value]?.let { it(this) }
            ?: (if (getIndex() in 2..3)
                properties[1][token.value]?.let { it(this) } ?: properties[0][token.value]?.let { it(this) }
            else properties[0][token.value]?.let { it(this) })
            ?: throw PositionalException("`${token.value}` not found", token)
    }

    /**
     * Does not include "this"
     */
    override fun getProperties(): PDictionary {
        val res = properties[0]
        if (getIndex() in 2..3)
            res.putAll(properties[1])
        res.putAll(properties[getIndex()])
        return PDictionary(res.mapValues { it.value(this) }.toMutableMap(), null)
    }


    override fun hasProperty(token: Token): Boolean {
        TODO("Not yet implemented")
    }

    override fun getFunction(token: Token)
            : Function =
        functions[getIndex()].find { it.name == token.value }
            ?: (if (getIndex() in 2..3)
                functions[1].find { it.name == token.value } ?: functions[0].find { it.name == token.value }
            else functions[0].find { it.name == token.value })
            ?: throw PositionalException("Primitive does not contain `${token.value}` function", token)

    override fun getFunctionOrNull(name: String) = functions[getIndex()].find { it.name == name }
        ?: (if (getIndex() in 2..3)
            functions[1].find { it.name == name } ?: functions[0].find { it.name == name }
        else functions[0].find { it.name == name })
//    private fun getPrimitiveIndex(): Int {
//        return when (this) {
//            is PArray -> 0
//            is PDouble -> 1
//            is PInt -> 2
//            is PString -> 3
//            else -> 4
//        }
//    }

    companion object {
        fun setProperty(primitive: Primitive, name: String, property: (s: Primitive) -> Property) {
            properties[primitive.getIndex()][name] = property
        }

        fun setFunction(primitive: Primitive, embeddedFunction: EmbeddedFunction) {
            functions[primitive.getIndex()].add(embeddedFunction)
        }

        val properties = List(7) { mutableMapOf<String, (s: Primitive) -> Property>() }
        val functions = List(7) { mutableListOf<Function>() }
        fun createPrimitive(value: Any, parent: Type? = null, token: Token = Token()): Primitive {
            return when (value) {
                is String -> PString(value, parent)
                is List<*> -> PArray(value as MutableList<Variable>, parent)
                is Int -> PInt(value, parent)
                is Double -> PDouble(value, parent)
                is MutableMap<*, *> -> PDictionary(value as MutableMap<out Any, out Variable>, parent)
                else -> throw PositionalException(
                    "cannot create variable of type `${value::class}`", token
                )
            }
        }
    }
}