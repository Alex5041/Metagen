import evaluation.Evaluation.evaluate
import lexer.Parser
import lexer.PositionalException
import lexer.SemanticAnalyzer
import properties.primitive.PArray
import token.Token
import java.io.File
import java.io.FileNotFoundException

fun main() {
//    val statements = readFile("constants")
//    println(statements.treeView())
//    evaluate(statements, "constants")
    val add = PArray(mutableListOf(),null)
    println(add.getFunctionOrNull("add"))
    val s = readFile("src/test/resources/testCode.redi")
    //println(s.treeView())
    evaluate(s, "testCode.redi")
}

fun readFile(path: String = "", tokenPath: Token = Token()): List<Token> {
    val file = File(if (path == "") tokenPath.value else path)
    val text: String
    try {
        text = file.readText()
    } catch (e: FileNotFoundException) {
        throw PositionalException("no import ${file.name} found", tokenPath)
    }

    val statements = Parser(text).statements()
    return SemanticAnalyzer(parseFilePath(path), statements).analyze()
}
fun parseFilePath(path:String):String = path.split("/").last()

//private fun createDefs() {
//    val root = OldContainer("Root", null, mutableMapOf())
//    //root.children.add(Property("type", root, "Line"))
//    root.declarations["type"] = Formula("@Line")
//    root.declarations["child"] = Formula("@Segment")
//    //root.declarations["child2"] = deprecated.Formula("@Segment")
//    root.declarations["iter"] = Formula("0")
//    root.declarations["x"] = Formula("20")
//    root.declarations["y"] = Formula("0")
//    root.declarations["x2"] = Formula("20")
//    root.declarations["y2"] = Formula("0")
//
//    //root.declarations["rotation"] = deprecated.Formula()
//
//    val segment = OldContainer("Segment", root, mutableMapOf())
//    //segment.children.add(Property("type", segment, "Line"))
//    segment.declarations["type"] = Formula("@Line")
//    segment.declarations["next"] = Formula("iter < 10 ? ({@randNum,0,1}>0.3 ? @Segment : @DoubleSegment) : @Nothing")
//    segment.declarations["iter"] = Formula("parent.iter + 1")
//    segment.declarations["x"] = Formula("parent.x2")
//    segment.declarations["y"] = Formula("parent.y2")
//    segment.declarations["x2"] = Formula("-{@sin,angle} * 10 + x")
//    segment.declarations["y2"] = Formula("{@cos,angle} * 10 + y")
//    segment.declarations["angle"] = Formula("{@randNum,-0.7,0.7}")
//
//    val doubleSegment = OldContainer("DoubleSegment", null, mutableMapOf())
//    doubleSegment.declarations["child"] = Formula("@Segment")
//    doubleSegment.declarations["child2"] = Formula("@Segment")
//    doubleSegment.declarations["x"] = Formula("parent.x2")
//    doubleSegment.declarations["y"] = Formula("parent.y2")
//    doubleSegment.declarations["x2"] = Formula("parent.x2")
//    doubleSegment.declarations["y2"] = Formula("parent.y2")
//    doubleSegment.declarations["iter"] = Formula("parent.iter + 1")
//
//    //segment.declarations["rotation"] = deprecated.Formula("{@randNum,-10,10}")
//
//    val nothing = OldContainer("Nothing", root, mutableMapOf())
//
//    TreeBuilder.definitions.addAll(mutableListOf(root, segment, nothing, doubleSegment))
//}