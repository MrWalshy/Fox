package dev.morganwalsh.fox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import dev.morganwalsh.fox.Expression.Array;
import dev.morganwalsh.fox.Expression.ArrayCall;
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

public class Resolver implements Expression.Visitor<Void> {

	private enum FunctionType {
		NONE, FUNCTION
	}

	private final Interpreter interpreter;

	/**
	 * Tracks the stack of scopes currently in scope.
	 * 
	 * Each element in the stack is a Map representing a single block scope.
	 * 
	 * Keys are variable names, booleans are values representing whether or not the
	 * variables initialiser has finished being resolved.
	 * 
	 * This stack is only used for local block scopes. Top-level declarations in the
	 * global scope are not tracked by the resolver.
	 * 
	 * When resolving a variable, if it is not found in the stack of local scopes it
	 * is assumed to be a global.
	 */
	private final Stack<Map<String, Boolean>> scopes;
	private FunctionType currentFunctionType = FunctionType.NONE;

	Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.scopes = new Stack<>();
	}

	void resolve(List<Expression> expressions) {
		for (Expression expression : expressions)
			resolve(expression);
	}

	void resolve(Expression expression) {
		expression.accept(this);
	}

	private void beginScope() {
		scopes.push(new HashMap<>());
	}

	private void endScope() {
		scopes.pop();
	}

	/**
	 * Add a variable to the innermost scope so it shadows any outer variables.
	 * 
	 * To mark the variable as uninitialised, it's name is bound to `false` in the
	 * scope map.
	 * 
	 * @param name
	 */
	private void declare(Token name) {
		if (scopes.isEmpty())
			return; // global

		Map<String, Boolean> scope = scopes.peek();
		if (scope.containsKey(name.lexeme))
			Fox.error(name, "A variable called " + name.lexeme + " has already been declared in the same scope.");
		scope.put(name.lexeme, false);
	}

	/**
	 * Defines that a given variable has been resolved.
	 * 
	 * @param name
	 */
	private void define(Token name) {
		if (scopes.isEmpty())
			return; // global
		scopes.peek().put(name.lexeme, true); // initialised variable
	}

	private void resolveLocalVariable(Expression expression, Token name) {
		// starting from the innermost scope
		for (int i = scopes.size() - 1; i >= 0; i--) {
			// look in the scope for the variable
			if (scopes.get(i).containsKey(name.lexeme)) {
				// resolve the variable, passing
				// - the number of scopes between the current innermost scope
				// and the scope where the variable was found. This
				// will be 0 if the variable is in the current scope, 1 in
				// the immediately enclosing scope, etc...
				interpreter.resolve(expression, scopes.size() - 1 - i);
				return;
			}
		}
		// if here is reached, variable is assumed to be global scope as
		// all block scopes have been resolved, meaning we leave it unresolved
	}
	
	private void resolveFunction(Function expression, FunctionType type) {
		FunctionType enclosingFunctionType = currentFunctionType;
		currentFunctionType = type;
		
		// create a scope
		beginScope();
		
		// bind the parameters
		for (Token param : expression.params) {
			declare(param);
			define(param);
		}
		
		// resolve the body of the function
		resolve(expression.body);
		
		endScope();
		currentFunctionType = enclosingFunctionType;
	}

	@Override
	public Void visitVarExpression(Var expression) {
		declare(expression.name);
		
		if (expression.initialiser != null) {
			resolve(expression.initialiser);
//			resolveLocalVariable(expression, expression.name);
		};
		define(expression.name);
		return null;
	}

	@Override
	public Void visitBlockExpression(Block expression) {
		beginScope();
		resolve(expression.expressions);
		endScope();
		return null;
	}

	@Override
	public Void visitTernaryExpression(Ternary expression) {
		resolve(expression.condition);
		resolve(expression.ifTrue);
		resolve(expression.ifFalse);
		return null;
	}

	@Override
	public Void visitBinaryExpression(Binary expression) {
		resolve(expression.left);
		resolve(expression.right);
		return null;
	}

	@Override
	public Void visitCallExpression(Call expression) {
		// resolve the identifier
		resolve(expression.callee);
		
		// walk and resolve arg list
		for (Expression arg : expression.arguments) resolve(arg);
		return null;
	}

	@Override
	public Void visitGroupingExpression(Grouping expression) {
		resolve(expression.expression);
		return null;
	}

	@Override
	public Void visitLiteralExpression(Literal expression) {
		return null;
	}

	@Override
	public Void visitLogicalExpression(Logical expression) {
		resolve(expression.left);
		resolve(expression.right);
		return null;
	}

	@Override
	public Void visitUnaryExpression(Unary expression) {
		resolve(expression.right);
		return null;
	}

	@Override
	public Void visitFunctionExpression(Function expression) {
		resolveFunction(expression, FunctionType.FUNCTION);
		return null;
	}

	@Override
	public Void visitVariableExpression(Variable expression) {
		// Check if the variable exists but has not been defined in
		// the current scope
		// - if so, report an error
		if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == Boolean.FALSE) {
			Fox.error(expression.name, "Can't read local variable in its own initialiser.");
		}

		// otherwise, resolve a local variable
		resolveLocalVariable(expression, expression.name);
		return null;
	}

	@Override
	public Void visitAssignExpression(Assign expression) {
		// Resolve the expression for the assigned value in case it contains references
		// to other variables
		resolve(expression.assignment);
		// resolve the variable being assigned to
		resolveLocalVariable(expression, expression.name);
		return null;
	}

	@Override
	public Void visitImportExpression(Import expression) {
		// compile-time static check of whether the file exists or not would be good
		// here, prevent run-time error of it not existing
		if (!Files.exists(Path.of(expression.file.literal.toString()))) {
			Fox.error(expression.file.line, "Cannot find file '" + expression.file.lexeme + "'.");
		}
		return null;
	}

	@Override
	public Void visitArrayExpression(Array expression) {
		resolve(expression.elements);
		return null;
	}

	@Override
	public Void visitArrayCallExpression(ArrayCall expression) {
		resolve(expression.callee);
		return null;
	}

}
