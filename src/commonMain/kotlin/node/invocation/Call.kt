package node.invocation

import lexer.PositionalException
import node.Node
import node.statement.Assignment
import properties.EmbeddedFunction
import properties.RFunction
import properties.Variable
import table.SymbolTable
import utils.Utils.toVariable

class Call(
    node: Node
) : Invocation(
    node.symbol, node.value,
    node.position,
    node.children
) {
    init {
        this.children.clear()
        this.children.addAll(node.children)
    }

    val allArgs = children.subList(1, children.size)
    val unnamedArgs: List<Node>
        get() {
            val res = mutableListOf<Node>()
            var i = 1
            while (i < children.size && children[i] !is Assignment) {
                res.add(children[i])
                i++
            }
            return res
        } // children.subList(1, children.size)
    val namedArgs: List<Assignment>
        get() = children.subList(unnamedArgs.size + 1, children.size) as List<Assignment>

    /**
     * For function evaluation, new scope table is needed, to which all arguments will be
     * passed, named like parameters
     */
    override fun evaluate(symbolTable: SymbolTable): Any {
        val function = symbolTable.getFunction(this)
        val newTable = symbolTable.changeFile(
            symbolTable.getFileOfFunction(this, function)
        ).changeScope()
        argumentsToParameters(function, symbolTable, newTable)
        return evaluateFunction(newTable, function)
    }

    /**
     * Write arguments to parameters
     */
    fun argumentsToParameters(function: RFunction, argTable: SymbolTable, paramTable: SymbolTable) {
        val assigned = mutableMapOf<String, Variable>()
        for ((index, arg) in unnamedArgs.withIndex()) {
            val paramName = getParamName(index, function)
            if (assigned[paramName] != null)
                throw PositionalException("Argument already assigned", arg)
            assigned[paramName] = arg.evaluate(argTable).toVariable(arg)
        }
        for (nArg in namedArgs) {
            if (!function.hasParam(nArg.name))
                throw PositionalException("Parameter with name `${nArg.name}` absent", nArg)
            if (assigned[nArg.name] != null)
                throw PositionalException("Argument already assigned", nArg)
            assigned[nArg.name] = nArg.right.evaluate(argTable).toVariable(nArg)
        }
        for (defaultParam in function.defaultParams)
            if (assigned[defaultParam.name] == null)
                assigned[defaultParam.name] = defaultParam.right.evaluate(paramTable).toVariable(defaultParam)
        for (param in function.nonDefaultParams)
            if (assigned[param.value] == null)
                throw PositionalException("Parameter not assigned", param, file = paramTable.getFileTable().fileName)
        for (i in assigned)
            paramTable.addVariable(i.key, i.value)
        // TODO exceptions only to calls, not to params
//        for ((index, arg) in arguments.withIndex())
//            paramTable.addVariable(function.params[index].value, arg.evaluate(argTable).toVariable(arg))
    }

    private fun getParamName(index: Int, function: RFunction): String {
        return if (function.nonDefaultParams.lastIndex < index) {
            if (function.nonDefaultParams.size + function.defaultParams.lastIndex < index)
                throw PositionalException("More arguments than parameters", children[index])
            else function.defaultParams[index - function.nonDefaultParams.size].name
        } else function.nonDefaultParams[index].value
    }

    fun evaluateFunction(symbolTable: SymbolTable, function: RFunction, argTable: SymbolTable? = null): Any {
        var argTable = argTable ?: symbolTable
        //   if (function.params.size < arguments.size)
        //      throw PositionalException("Expected less arguments", this)
        // wtf
//        if (symbolTable.getVariableOrNull("this") != null)
//            symbolTable.addVariable("this", symbolTable.getVariable("this"))
//        if (symbolTable.getCurrentType() != null)
//            symbolTable.addVariable("this", symbolTable.getCurrentType()!!)

        val res = if (function is EmbeddedFunction)
            function.executeFunction(left, symbolTable)
        else function.body.evaluate(symbolTable)
        // this is because Unit variables are not allowed and in Links variable is assigned
        return if (res is Unit) 0 else res
    }
}
