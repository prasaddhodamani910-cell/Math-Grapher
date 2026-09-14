package com.prasad.mathgrapher.math

object EquationClassifier {
    private val STANDALONE_T = Regex("(?<![a-zA-Z])t(?![a-zA-Z])")

    fun classify(input: String): EquationType {
        // Sanitize
        var str = input.replace("−", "-")
            .replace("²", "^2")
            .trim()
            
        // 2. Polar
        if (str.startsWith("r =") || str.startsWith("r=")) {
            val rhsStr = str.substring(str.indexOf("=") + 1).trim()
            return EquationType.Polar(parse(rhsStr))
        }
        
        // 3. Parametric
        if (str.contains(",") && STANDALONE_T.containsMatchIn(str)) {
            val parts = str.split(",", limit = 2)
            if (parts.size == 2) {
                var xPart = parts[0].trim()
                var yPart = parts[1].trim()
                
                if (xPart.startsWith("x =") || xPart.startsWith("x=")) {
                    xPart = xPart.substring(xPart.indexOf("=") + 1).trim()
                }
                if (yPart.startsWith("y =") || yPart.startsWith("y=")) {
                    yPart = yPart.substring(yPart.indexOf("=") + 1).trim()
                }
                
                return EquationType.Parametric(parse(xPart), parse(yPart))
            }
        }
        
        // 4. Inequality
        val inequalities = listOf("<=", ">=", "<", ">")
        for (opStr in inequalities) {
            if (str.contains(opStr)) {
                val parts = str.split(opStr, limit = 2)
                val lhsAst = parse(parts[0])
                val rhsAst = parse(parts[1])
                val op = when (opStr) {
                    "<=" -> CompareOp.LESS_EQUAL
                    ">=" -> CompareOp.GREATER_EQUAL
                    "<" -> CompareOp.LESS
                    ">" -> CompareOp.GREATER
                    else -> error("Impossible")
                }
                return EquationType.Inequality(lhsAst, rhsAst, op)
            }
        }
        
        // 5. Equals
        if (str.contains("=")) {
            if (str.startsWith("y =") || str.startsWith("y=")) {
                val rhsStr = str.substring(str.indexOf("=") + 1).trim()
                return EquationType.Explicit(parse(rhsStr))
            }
            
            val parts = str.split("=", limit = 2)
            val lhsAst = parse(parts[0])
            val rhsAst = parse(parts[1])
            
            val hasY = containsVar(lhsAst, "y") || containsVar(rhsAst, "y")
            if (!hasY) {
                // If it doesn't use y, we treat it as Explicit (e.g. solving for x, or just graphing y = lhs - rhs)
                // However, the instructions say "If the expression only uses x (no y), return EquationType.Explicit(expr)."
                // We'll wrap it in a subtraction so it graphs correctly as y = lhs - rhs
                return EquationType.Explicit(AstNode.BinOp('-', lhsAst, rhsAst))
            } else {
                return EquationType.Implicit(lhsAst, rhsAst)
            }
        }
        
        // 6. Default explicit
        return EquationType.Explicit(parse(str))
    }
    
    private fun parse(expression: String): AstNode {
        val tokenizer = Tokenizer(expression.trim())
        val tokens = tokenizer.tokenize()
        val parser = Parser(tokens)
        return parser.parse()
    }
    
    private fun containsVar(node: AstNode, varName: String): Boolean {
        return when (node) {
            is AstNode.Num -> false
            is AstNode.Var -> node.name == varName
            is AstNode.BinOp -> containsVar(node.left, varName) || containsVar(node.right, varName)
            is AstNode.UnaryOp -> containsVar(node.operand, varName)
            is AstNode.FuncCall -> containsVar(node.arg, varName)
        }
    }
}
