package dev.morganwalsh.fox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import dev.morganwalsh.fox.Expression.Assign;
import dev.morganwalsh.fox.Expression.Binary;
import dev.morganwalsh.fox.Expression.Block;
import dev.morganwalsh.fox.Expression.Call;
import dev.morganwalsh.fox.Expression.Function;
import dev.morganwalsh.fox.Expression.Grouping;
import dev.morganwalsh.fox.Expression.Import;
import dev.morganwalsh.fox.Expression.Literal;
import dev.morganwalsh.fox.Expression.Logical;
import dev.morganwalsh.fox.Expression.Ternary;
import dev.morganwalsh.fox.Expression.Unary;
import dev.morganwalsh.fox.Expression.Var;
import dev.morganwalsh.fox.Expression.Variable;
import dev.morganwalsh.fox.native_functions.Clock;
import dev.morganwalsh.fox.native_functions.Evaluate;
import dev.morganwalsh.fox.native_functions.Length;
import dev.morganwalsh.fox.native_functions.io.CharacterToString;
import dev.morganwalsh.fox.native_functions.io.GetCharacter;
import dev.morganwalsh.fox.native_functions.io.Input;
import dev.morganwalsh.fox.native_functions.io.Print;

public class Interpreter implements Expression.Visitor<Object> {
	
	private final Scanner sc = new Scanner(System.in);
	
	/**
	 * Holds all global variables and functions in a contained environment, this is
	 * the parent environment of all spawned environments and thus implicitly gives them
	 * access to contained variables and functions.
	 */
	private final Environment globals = new Environment();
	
	/**
	 * Represents the current environment, with a reference to its parent environment.
	 */
	private Environment currentEnvironment = globals;
	
	/**
	 * Stores local variable resolution information by associating each syntax 
	 * tree node with the location of its data.
	 * 
	 * The location is represented as the number of hops away the data is, in this
	 * case hops represent how many enclosing environments away it is.
	 * 
	 * Where a variable is declared in the global environment, it is not
	 * added to this map.
	 */
	private final Map<Expression, Integer> locals = new HashMap<>();
	
	public Interpreter() {
		globals.define("clock", new Clock());
		globals.define("input", new Input());
		globals.define("len", new Length());
		globals.define("eval", new Evaluate());
		globals.define("print", new Print());
		globals.define("getch", new GetCharacter());
		globals.define("charToStr", new CharacterToString());
	}

	String interpret(List<Expression> expressions) {
		try {
			for (int i = 0; i < expressions.size() - 1; i++) interpret(expressions.get(i));
			return stringify(interpret(expressions.get(expressions.size() - 1)));
		} catch (RuntimeError error) {
			Fox.runtimeError(error);
			return null;
		}
	}

	Object interpret(Expression expression) {
		return expression.accept(this);
	}
	
	public Object interpretScopedBlock(Expression body, Environment env) {
		Environment previous = currentEnvironment;
		currentEnvironment = env;
		Object output = interpret(body);
		currentEnvironment = previous;
		return output;
	}
	
	private String stringify(Object value) {
		if (value == null) return "null";
		
		if (value instanceof Double) {
			String text = value.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		return value.toString();
	}

	@Override
	public Object visitVarExpression(Var expression) {
		Object value = null;
		
		if (expression.initialiser != null) {
			value = interpret(expression.initialiser);
			currentEnvironment.define(expression.name.lexeme.toString(), value);
		} else {
			currentEnvironment.define(expression.name.lexeme.toString(), null);
		}
		return value;
	}

	@Override
	public Object visitBlockExpression(Block expression) {
		Environment enclosing = currentEnvironment;
		Object output = null;
		
		try {
			currentEnvironment = new Environment(enclosing);
			for (int i = 0; i < expression.expressions.size() - 1; i++) interpret(expression.expressions.get(i));
			Expression tail = expression.expressions.get(expression.expressions.size() - 1);
			output = interpret(tail);
		} finally {
			// restore the enclosing environment after scope exits
			currentEnvironment = enclosing;
		}
		return output;
	}

	@Override
	public Object visitTernaryExpression(Ternary expression) {
//		Object ifFalse = interpret(expression.ifFalse);
//		Object ifTrue = interpret(expression.ifTrue);
//		Object conditionResult = interpret(expression.condition);
//		
//		if (isTruthy(conditionResult)) return ifTrue;
//		else return ifFalse;
		// only eval the needed path to prevent infinite recursion
		if (isTruthy(interpret(expression.condition))) return interpret(expression.ifTrue);
		else return interpret(expression.ifFalse);
	}

	@Override
	public Object visitBinaryExpression(Binary expression) {
		Object left = interpret(expression.left);
		Object right = interpret(expression.right);
		
		switch (expression.operator.type) {
		case MINUS:
			checkNumberOperands(expression.operator, left, right);
			return (double)left - (double)right;
		case SLASH:
			checkNumberOperands(expression.operator, left, right);
			if ((double)right == 0) throw new RuntimeError(expression.operator, "Cannot divide by zero");
			return (double)left / (double)right;
		case STAR:
			checkNumberOperands(expression.operator, left, right);
			return (double)left * (double)right;
		case GREATER:
			checkNumberOperands(expression.operator, left, right);
			return (double)left > (double)right;
		case GREATER_EQUAL:
			checkNumberOperands(expression.operator, left, right);
			return (double)left >= (double)right;
		case LESS:
			checkNumberOperands(expression.operator, left, right);
			return (double)left < (double)right;
		case LESS_EQUAL:
			checkNumberOperands(expression.operator, left, right);
			return (double)left <= (double)right;
		case BANG_EQUAL:
			return !isEqual(left, right);
		case EQUAL_EQUAL:
			return isEqual(left, right);
		case PLUS:
			if (left instanceof Double && right instanceof Double) {
				return (double)left + (double)right;
			} else if (left instanceof String) {
				return (String)left + stringify(right);
			} else if (right instanceof String) {
				return stringify(left) + (String)right;
			}
			throw new RuntimeError(expression.operator, "Operands must be two numbers or strings");
		}
		return null;
	}

	@Override
	public Object visitCallExpression(Call expression) {
		// Evaluate the callee (typically an identifier for a function
		Object callee = interpret(expression.callee);
		
		// Evaluate the arguments
		List<Object> arguments = new ArrayList<>();
		for (Expression arg : expression.arguments) arguments.add(interpret(arg));
		
		// Check if the callee object is an instance of LoxCallable
		if (!(callee instanceof FoxCallable)) {
			throw new RuntimeError(expression.closingParenthesis, "Can only invoke functions.");
		}
		
		// cast callable object to correct type
		FoxCallable callable = (FoxCallable)callee;
		
		// check parameter count
		if (arguments.size() != callable.arity()) {
			throw new RuntimeError(expression.closingParenthesis, "Expected " + callable.arity() + " arguments but got " + arguments.size() + ".");
		}
		return callable.call(this, arguments, expression.closingParenthesis);
	}

	@Override
	public Object visitGroupingExpression(Grouping expression) {
		return interpret(expression.expression);
	}

	@Override
	public Object visitLiteralExpression(Literal expression) {
		return expression.value;
	}

	@Override
	public Object visitLogicalExpression(Logical expression) {
		Object left = interpret(expression.left);
		
		switch (expression.operator.type) {
		case OR:
			if (isTruthy(left)) return left; // short circuit
			break;
		case AND:
			if (!isTruthy(left)) return left;
			break;
		default:
			break;
		}
		return interpret(expression.right);
	}

	@Override
	public Object visitUnaryExpression(Unary expression) {
		// Right-recursion, eval in post-order traversal
		Object right = interpret(expression.right);

		switch (expression.operator.type) {
		case MINUS:
			checkNumberOperand(expression.operator, right);
			return -(double)right;
		case BANG:
			return !isTruthy(right);
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitFunctionExpression(Function expression) {
		FoxFunction function = null;
		if (expression.identifier != null) {
			// pass the new function the scope of where it was declared
			function = new FoxFunction(expression.identifier.lexeme, expression, currentEnvironment);
			currentEnvironment.define(expression.identifier.lexeme, function);
		} else function = new FoxFunction(null, expression, currentEnvironment);
		return function;
	}

	@Override
	public Object visitVariableExpression(Variable expression) {
		return lookupVariable(expression.name, expression);
	}
	
	@Override
	public Object visitAssignExpression(Assign expression) {
		Object value = interpret(expression.assignment);
		
		Integer hops = locals.get(expression);
		
		if (hops != null) currentEnvironment.assignAt(hops, expression.name, value);
		else globals.assign(expression.name, value);
		return value;
	}

	private Object lookupVariable(Token name, Variable expression) {
		// Get the number of hops (enclosing environments) away the value is
		Integer hops = locals.get(expression);
		
		if (hops != null) return currentEnvironment.getAt(hops, name.lexeme);
		else return globals.get(name); // dynamic lookup
	}

	private boolean isTruthy(Object obj) {
		if (obj == null) return false;
		if (obj instanceof Boolean) return (boolean) obj;
		return true;
	}
	
	private boolean isEqual(Object left, Object right) {
		if (left == null && right == null) return true;
		if (left == null) return false;
		return left.equals(right);
	}
	
	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}
	
	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be a number.");
	}

	/**
	 * Used by the resolver to tell the interpreter the number of hops necessary 
	 * to find the correct scope for a given variable definition.
	 * @param expression
	 * @param hops
	 */
	public void resolve(Expression expression, int hops) {
		locals.put(expression, hops);
	}

	@Override
	public Object visitImportExpression(Import expression) {
		Object output = null;
		try {
			String src = Files.readString(Path.of(expression.file.literal.toString()));
			output = Fox.evaluate(src);
		} catch (IOException e) {
			throw new RuntimeError(expression.file, "Something went wrong trying to access '" + expression.file.literal + "'.");
		}
		return output;
	}
	
}
