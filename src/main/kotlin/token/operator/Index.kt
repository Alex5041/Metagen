package token.operator

import Optional
import lexer.ExpectedTypeException
import lexer.Parser
import lexer.PositionalException
import properties.Type
import properties.Variable
import properties.primitive.*
import table.SymbolTable
import token.Assignable
import token.Linkable
import token.Token
import token.statement.Assignment
import utils.Utils.toVariable

/**
 * Format: `a[i]` -  `[]` is index, `a` is indexed value
 *  ([PArray] or [PDictionary]) and `i` is [PInt] or key of dictionary.
 *
 * Token that represents taking value from collection by index or key
 */
class Index(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: (
        (
        token: Token, parser: Parser, token2: Token
    ) -> Token
    )?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std), Assignable, Linkable {
    constructor(token: Token) : this(
        token.symbol,
        token.value,
        token.position,
        token.bindingPower,
        token.nud,
        token.led,
        token.std,
        token.children
    )

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val res = evaluateIndex(symbolTable)
        if (res is Primitive)
            return res.getPValue()
        return res
    }

    fun evaluateIndex(symbolTable: SymbolTable): Any {
        val indexed = left.evaluate(symbolTable).toVariable(left)
        val index = right.evaluate(symbolTable)
        return when (indexed) {
            is Indexable -> indexed[index, right]
            else -> throw ExpectedTypeException(listOf(PArray::class, PDictionary::class, PString::class), this, this)
        }
    }

    private fun getIndexableAndIndex(symbolTable: SymbolTable): Pair<Indexable, Variable> {
        val indexable = symbolTable.getIdentifier(left)
        val index = right.evaluate(symbolTable).toVariable(right)
        if (indexable is Indexable && indexable.checkIndexType(index))
            return Pair(indexable, index)
        throw ExpectedTypeException(listOf(PArray::class, Number::class), this, expectedMultiple = true)
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        if (parent == null || parent.getProperty(getPropertyName()) == PInt(0, parent)) {
            val (arr, ind) = getIndexableAndIndex(symbolTable)
            arr.set(ind, value.toVariable(assignment.right), left, right)
            return
        }
        val property = parent.getProperty(getPropertyName())
        if (property !is Indexable)
            throw ExpectedTypeException(listOf(PArray::class, PDictionary::class, PString::class), left, property)
        val index = right.evaluate(symbolTable)
        if (index !is Int)
            throw PositionalException("Index is not integer", this)
        property.set(index, right.evaluate(symbolTable).toVariable(right), left, right)
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?> {
        val fromAnother = (left as Assignable).getFirstUnassigned(parent, symbolTable)
        if (fromAnother.second != null) return fromAnother
        val indexUnassigned =
            right.traverseUntilOptional {
                if (it is Assignable
                    && it.getFirstUnassigned(parent, symbolTable).second != null
                ) Optional(it) else Optional()
            }
        if (indexUnassigned.value != null) return (indexUnassigned.value as Assignable).getFirstUnassigned(
            parent,
            symbolTable
        )
        if (parent.getAssignment(this) != null)
            return Pair(parent, parent.getAssignment(this))
        return Pair(parent, null)
    }

    override fun getPropertyName(): Token = (left as Assignable).getPropertyName()
}
