package dev.morganwalsh.fox;

import java.util.List;

import dev.morganwalsh.fox.Expression.Array;
import dev.morganwalsh.fox.Expression.ArrayCall;
import dev.morganwalsh.fox.Expression.Assign;
import dev.morganwalsh.fox.Expression.Binary;
import dev.morganwalsh.fox.Expression.Block;
import dev.morganwalsh.fox.Expression.Call;
import dev.morganwalsh.fox.Expression.Case;
import dev.morganwalsh.fox.Expression.CasePattern;
import dev.morganwalsh.fox.Expression.Function;
import dev.morganwalsh.fox.Expression.Grouping;
import dev.morganwalsh.fox.Expression.Import;
import dev.morganwalsh.fox.Expression.Literal;
import dev.morganwalsh.fox.Expression.Logical;
import dev.morganwalsh.fox.Expression.Match;
import dev.morganwalsh.fox.Expression.Ternary;
import dev.morganwalsh.fox.Expression.Unary;
import dev.morganwalsh.fox.Expression.Var;
import dev.morganwalsh.fox.Expression.Variable;

public class AstPrinter implements Expression.Visitor<String> {
	
	public static void main(String[] args) {
		String blockTest = "{ (1 + 2) * 3, 4 + 4 }";
		String varTest = "var (\"x\", {32 + 5, 22 + 5})";
		String funCallTest = "test(x, 55, 3 + 3)";
		String funDefinitionTest = "defun (\"x\", (a,b,c) -> 3 + 3)";
		String ternaryTest = "var (\"y\", {"
				+ 		"x > 3 ? \"Yes\" : \"No\","
				+ 		"print(x)"
				+ "})";
		String testExpressionBeforeTernary = "3 + 3 + ";
		String ternaryTestSolo = "x > 3 ? \"Yes\" : \"No\"";
		String testExpressionAfterTernary = "assign(x, 32)";
		
		String returnAnonFunctionTest = "defun (\"getFunc\", () -> (y) -> print(y))";
		
		String multipleExpressionTest = blockTest + varTest + funCallTest + funDefinitionTest + ternaryTest + testExpressionBeforeTernary + ternaryTestSolo + testExpressionAfterTernary;
		
		Tokeniser sc = new Tokeniser(multipleExpressionTest);
		Parser parser = new Parser(sc.scanTokens());
		List<Expression> parsed = parser.parse();
		
		new AstPrinter().print(parsed);
	}

	private void print(List<Expression> expressions) {
		for (Expression expression : expressions) {
			System.out.println(expression.accept(this));
		}
	}
	
	private String parenthesize(String name, Expression... expressions) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("(").append(name);
		
		for (Expression expr : expressions) {
			sb.append(" ");
			sb.append(expr.accept(this)); // recursive step for printing the whole tree
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String visitVarExpression(Var expression) {
		return parenthesize(expression.name.lexeme, expression.initialiser);
	}

	@Override
	public String visitBlockExpression(Block expression) {
		String output = "( block ";
		for (int i = 0; i < expression.expressions.size(); i++) {
			output += "\n\t" + expression.expressions.get(i).accept(this);
			if (i < expression.expressions.size() - 1) output += ", \t";
		}
		output += ") ";
		return output;
	}

	@Override
	public String visitTernaryExpression(Ternary expression) {
		String condition = parenthesize("?", expression.condition);
		String result = parenthesize(":", expression.ifTrue, expression.ifFalse);
		return "(" + condition + " (" + result + "))";
	}

	@Override
	public String visitBinaryExpression(Binary expression) {
		return parenthesize(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitCallExpression(Call expression) {
		String output = "(call ";
		output += expression.callee.accept(this) + "("; // func name
		
		if (expression.arguments.size() > 0) {
			output += expression.arguments.get(0).accept(this);
			for (int i = 1; i < expression.arguments.size(); i++) {
				output += ", " + expression.arguments.get(i).accept(this);
			}
		}
		
		output += "))";
		return output;
	}

	@Override
	public String visitGroupingExpression(Grouping expression) {
		return parenthesize("group", expression.expression);
	}

	@Override
	public String visitLiteralExpression(Literal expression) {
		if (expression.value == null) return "null";
		return expression.value.toString();
	}

	@Override
	public String visitLogicalExpression(Logical expression) {
		return parenthesize(expression.operator.lexeme, expression.left, expression.right);
	}

	@Override
	public String visitUnaryExpression(Unary expression) {
		return parenthesize(expression.operator.lexeme, expression.right);
	}

	@Override
	public String visitFunctionExpression(Function expression) {
		String name = expression.identifier != null ? expression.identifier.lexeme : "<anon>";
		String output = "(defun " + name + " \n";
		output += "  (parameters (";
		for (int i = 0; i < expression.params.size(); i++) {
			output += expression.params.get(i).lexeme;
			if (i < expression.params.size() - 1) {
				output += ", ";
			}
		}
		output += "))\n  ";
		output += parenthesize("body", expression.body);
		output += "\n)";
		return output;
	}

	@Override
	public String visitVariableExpression(Variable expression) {
		return expression.name.lexeme;
	}

	@Override
	public String visitAssignExpression(Assign expression) {
		return parenthesize(expression.name.lexeme, expression.assignment);
	}

	@Override
	public String visitImportExpression(Import expression) {
		return parenthesize("import " + expression.file.lexeme);
	}

	@Override
	public String visitArrayExpression(Array expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitArrayCallExpression(ArrayCall expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitMatchExpression(Match expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitCaseExpression(Case expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitCasePatternExpression(CasePattern expression) {
		// TODO Auto-generated method stub
		return null;
	}

}
