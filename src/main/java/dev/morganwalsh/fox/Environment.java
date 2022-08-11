package dev.morganwalsh.fox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	
	final Environment enclosing;
	private final Map<String, Object> variables = new HashMap<>();
	
	public Environment() {
		this(null);
	}

	public Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	public void define(String name, Object value) {
		variables.put(name, value);
	}
	
	public void assign(Token name, Object value, Object arrayIndex) {	
		// this environment
		if (variables.containsKey(name.lexeme)) {
			// if literal is a number, its an array being assigned to
			if (arrayIndex instanceof Double) {
				Object[] arr = (Object[]) variables.get(name.lexeme);
				int index = ((Double) arrayIndex).intValue();
				arr[index] = value;
				return;
			}
			variables.put(name.lexeme, value);
			return;
		}
		
		// enclosing environment
		if (enclosing != null) {
			enclosing.assign(name, value, arrayIndex);
			return;
		}
		
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
	
	public void assignAt(Integer hops, Token name, Object value, Object arrayIndex) {
		Environment env = ancestor(hops);
		
		// is it an array
		if (arrayIndex instanceof Double) {
			Object[] arr = (Object[]) variables.get(name.lexeme);
			int index = ((Double) arrayIndex).intValue();
			arr[index] = value;
			return;
		}
		env.variables.put(name.lexeme, value);
	}
	
	private Environment ancestor(Integer hops) {
		Environment currentEnvironment = this;
		
		for (int i = 0; i < hops; i++) {
			currentEnvironment = currentEnvironment.enclosing;
		}
		return currentEnvironment;
	}

	public Object getAt(Integer hops, String name) {
		return ancestor(hops).variables.get(name);
	}

	public Object get(Token name) {
		if (variables.containsKey(name.lexeme)) return variables.get(name.lexeme);
		if (enclosing != null) return enclosing.get(name);
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}

}

//> var("x", { var("weather", "sunny"), weather == "sunny" ? "No need for a brolly, it's sunny" : "Get a raincoat" })
//Undefined variable 'weather'.
//[line 0]
//null