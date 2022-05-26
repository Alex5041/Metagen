import evaluation.Evaluation
import evaluation.Evaluation.evaluate
import lexer.Parser
import lexer.PositionalException
import lexer.SemanticAnalyzer
import token.Token
import java.io.File
import java.io.FileNotFoundException

fun main() {
//    val statements = readFile("constants")
//    println(statements.treeView())
//    evaluate(statements, "constants")
//    initializeEmbeddedArrayFunctions()
//    initializeEmbeddedStringFunctions()
//
//    initializeIntProperties()
//    initializeDoubleProperties()
//    initializeArrayProperties()
//    initializeStringProperties()
    val t = Parser("a = b.size")
    val a = t.statements()[0]
    val s = readFile("src/test/resources/testCode.redi")
    SemanticAnalyzer.initializeSuperTypes()
    println(Evaluation.globalTable)
    evaluate(s, "testCode.redi")
}

fun readFile(path: String = "", tokenPath: Token = Token()): List<Token> {
    val file = File(if (path == "") tokenPath.value else if (path.contains(".")) path else "$path.redi")
    val text: String
    try {
        text = file.readText()
    } catch (e: FileNotFoundException) {
        throw PositionalException("no import `${file.name}` found", tokenPath)
    }
    val statements = Parser(text).statements()
    // println(statements.treeView())
    return SemanticAnalyzer(parseFilePath(path), statements).analyze()
}

fun parseFilePath(path: String): String = path.split("/").last()