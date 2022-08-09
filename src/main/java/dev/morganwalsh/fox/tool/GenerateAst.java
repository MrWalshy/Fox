package dev.morganwalsh.fox.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		String outputDir = args[0];
		// Grammar implementation for Lox
		defineAst(outputDir, "Expression", Arrays.asList(
			  "Var      : Token name, Expression initialiser",
			  "Block    : List<Expression> expressions",
			  "Ternary  : Expression condition, Expression ifTrue, Expression ifFalse, Token operator",
		      "Binary   : Expression left, Token operator, Expression right",
		      "Call     : Expression callee, Token closingParenthesis, List<Expression> arguments",
		      "Grouping : Expression expression",
		      "Literal  : Object value",
		      "Logical  : Expression left, Token operator, Expression right",
		      "Unary    : Token operator, Expression right",
		      "Function : Token identifier, List<Token> params, Expression body",
		      "Variable : Token name",
		      "Assign   : Token name, Expression assignment",
		      "Import   : Token file",
		      "Array    : List<Expression> elements, Token closingBracket",
		      "ArrayCall : Expression callee, Token index, Token upperBound, Token closingBracket",
		      "Match    : Token matchToken, Expression value, List<Case> cases",
		      "Case     : Token caseToken, Expression condition, Expression body",
		      "CasePattern : Expression left, Token operator, Expression right",
		      "While    : Expression condition, Expression body",
		      "ControlFlow : Token keyword"
	    ));
	}

	private static void defineAst(String outputDir, String baseName, List<String> types) throws FileNotFoundException, UnsupportedEncodingException {
		String path = outputDir + "/" + baseName + ".java";
	    PrintWriter writer = new PrintWriter(path, "UTF-8");

	    writer.println("package dev.morganwalsh.fox;");
	    writer.println();
	    writer.println("import java.util.List;");
	    writer.println();
	    writer.println("public abstract class " + baseName + " {");

	    defineVisitor(writer, baseName, types);
	    
	    // The AST classes.
	    for (String type : types) {
	      String className = type.split(":")[0].trim();
	      String fields = type.split(":")[1].trim(); 
	      defineType(writer, baseName, className, fields);
	    }
	    
	    // The base accept() method.
	    writer.println();
	    writer.println("  abstract <R> R accept(Visitor<R> visitor);");

	    
	    writer.println("}");
	    writer.close();
	}

	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("  interface Visitor<R> {");

	    for (String type : types) {
	      String typeName = type.split(":")[0].trim();
	      writer.println("    R visit" + typeName + baseName + "(" +
	          typeName + " " + baseName.toLowerCase() + ");");
	    }

	    writer.println("  }");
	}

	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		writer.println("  static class " + className + " extends " +
		        baseName + " {");

	    // Constructor.
	    writer.println("    " + className + "(" + fieldList + ") {");

	    // Store parameters in fields.
	    String[] fields = fieldList.split(", ");
	    for (String field : fields) {
	      String name = field.split(" ")[1];
	      writer.println("      this." + name + " = " + name + ";");
	    }

	    writer.println("    }");
	    
	    // Visitor pattern.
	    writer.println();
	    writer.println("    @Override");
	    writer.println("    <R> R accept(Visitor<R> visitor) {");
	    writer.println("      return visitor.visit" +
	        className + baseName + "(this);");
	    writer.println("    }");

	    // Fields.
	    writer.println();
	    for (String field : fields) {
	      writer.println("    final " + field + ";");
	    }

	    writer.println("  }");
	}
}
