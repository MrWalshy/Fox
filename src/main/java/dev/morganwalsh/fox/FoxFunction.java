package dev.morganwalsh.fox;

import java.util.List;

import dev.morganwalsh.fox.Expression.Function;

public class FoxFunction implements FoxCallable {

	private final String name;
	private final Expression.Function function;
	private final Environment closure;

	public FoxFunction(String name, Function function, Environment closure) {
		super();
		this.name = name;
		this.function = function;
		this.closure = closure;
	}

	// Each function has its own environment (scope)
	// - sharing environments between functions would break recursion.
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		// The closure holds a reference to its enclosing environment, and any variables
		// declared within its own environment
		Environment env = new Environment(closure);

		for (int i = 0; i < function.params.size(); i++) {
			env.define(function.params.get(i).lexeme, arguments.get(i));
		}
		return interpreter.interpretScopedBlock(function.body, env);
	}

	@Override
	public int arity() {
		// When the parameters are bound, it is assumed the parameter
		// and argument lists have the same length.
		// - safe as visitCallExpression() checks the arity before calling call()
		return function.params.size();
	}

	@Override
	public String toString() {
		if (name == null) return "<fn>";
		return "<fn " + name + ">";
	}
}
