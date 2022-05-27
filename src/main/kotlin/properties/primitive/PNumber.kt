package properties.primitive

import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Type
import token.Token
import utils.Utils.unaryMinus
import utils.Utils.unifyNumbers
import utils.Utils.unifyPNumbers

abstract class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 1
    override fun getPValue() = value as Number
    override fun getFunction(token: Token): Function =
        Primitive.functions[getIndex()].find { it.name == token.value }
            ?: functions.find { it.name == token.value }
            ?: throw PositionalException("Number does not contain `${token.value}` function", token)


    fun setFunction(embeddedFunction: EmbeddedFunction) {
        Primitive.functions[1].add(embeddedFunction)
        Primitive.functions[2].add(embeddedFunction)
    }

    companion object {
        val functions = initializeEmbeddedNumberFunctions()

        private fun initializeEmbeddedNumberFunctions(): MutableList<Function> {
            val res = mutableListOf<Function>()
            res.add(EmbeddedFunction("abs", listOf()) { token, args ->
                val number = args.getPropertyOrNull("this")!!
                if (number is PNumber)
                    if (number.getPValue().toDouble() >= 0) number.getPValue() else -number.getPValue()
                else throw PositionalException("Expected number", token)
            })
            res.add(EmbeddedFunction("min", listOf(Token(value = "other"))) { token, args ->
                val (number, other) = unifyNumbers(
                    args.getPropertyOrNull("this")!!,
                    args.getVariable("other"),
                    token
                )
                if (number is Int)
                    number.coerceAtMost(other as Int)
                else (number as Double).coerceAtMost(other as Double)
            })
            res.add(EmbeddedFunction("max", listOf(Token(value = "other"))) { token, args ->
                val (number, other) = unifyPNumbers(
                    args.getPropertyOrNull("this")!!,
                    args.getVariable("other"),
                    token
                )
                if (number is Int)
                    number.coerceAtLeast(other as Int)
                else (number as Double).coerceAtLeast(other as Double)
            })
            return res
        }
    }
}