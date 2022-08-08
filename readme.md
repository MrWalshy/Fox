# Fox

Fox, a dynamically typed, interpreted expression-based programming language.

In Fox, everything is an expression and thus returns a value.

## Example script

A quick example to show what Fox is capable of:

```fox

```

## Language reference

### Data types

Fox supports only a limited number of data types, specifically:

- number

- string

- boolean

- null

These are the core atomic data types. Each of these types resolve to a value.

#### number

A number is simple to use:

```
0
```

Numbers can be whole or floating-point:

```
0.0
```

#### string

A string is a sequence of characters wrapped in double quotes:

```
"Hello world"
```

The newline escape character is also valid in a string:

```
"Hello\nWorld"
```

- if output to the console, it will be split over two lines

As `\` is the escape character, you can use it to escape itself in text:

```
"Hello\\World"
```

#### boolean

A boolean is a truthy or falsey value. In Fox, `null` and `\0` are considered falsey, everything else is truthy.

Boolean values are represented using `true` or `false`.

### Variables

A variable is a container for your data, use the built-in `var` function to declare a new variable:

```
var(identifier, "value")
```

A variable defined in the global scope will be accessible everywhere in the program. A new lexical scope can be created using curly braces:

```
var(global, "global variable")

{
  var(inner, "inner variable")
}

print(global) // global variable
print(inner) // runtime error
```

A variable can be redeclared in the global scope:

```
var(global, 1)
var(global, 2)
```

But a variable cannot be redeclared in a local scope:

```
{
  var(scoped, 22)
  var(scoped, 30) // static analysis error
}
```

A variable also cannot reference itself when being defined:

```
{
  var(scoped, scoped) // runtime error
}
```

### Expressions

In Fox, everything is an expression. This means even `var` calls return a value:

```
var(x, var(y, 32))
print(x) // 32
```

Expressions are also not delimited by a semi-colon like most languages - this is due to the expression based nature of the language allowing for it.

An expression block can be used to create a new local lexical scope:

```
{
  var(x, 32)
}
```

We can also specify multiple expressions in an expression block:

```
{
  var(x, 32)
  x + 5
}
```

In an expression block, the result of the last expression is returned. In the last example, the expression block returns `37` and could be used in a variable assignment:

```
var(a, {
  var(x, 50)
  x + 5
})
```

In this case, `a` would be set to `55`, the value returned from the expression block.

### Function definitions

A new function can be defined using the `defun` expression:

```
defun(println, (str) -> {
  print(str + "\n")
})
```

Function definition expressions accept an identifier as the first parameter followed by a lambda function representing the function.

The parenthesis are the formal parameters of the function whilst everything after the `->` is the body.

It is not necessary to use a block for a function definition:

```
defun(println, (str) -> print(str + "\n"))
```

Function expressions can also be created, which can then be passed around like any other first class member:

```
var(doSomething, '() -> print("Hello"))

// call it
doSomething() // Hello
```

### Import expression

The `import` expression is used to import a file and execute its contents in the given scope:

```
import("some/file/location")
```

To prevent a runtime-error occurring, an error will be thrown during static analysis (compile-time) by the `Resolver`.

Once you have imported a file, its top-level functions and variables will be available to you.

If this file is a script, you could also capture its output:

```
var(x, import("some/file/location"))
```

This will capture the output of the last expression in the file. It will also evaluate its contents in the scope it is called in.

```
var(x, "original")
{
  var(x, import("some/file/location"))
}
print(x) // original
```

As can be seen above, the `x` declared in the outer scope retains its original value.

The `import` expression can also be used to import a function directly from a file into a variable. Given a file called `add.fox` containing:

```
'(a,b) -> a + b
```

When imported, it can be assigned to a variable and then used like a normal function (this also works with named functions):

```
var(add, import("src/main/resources/add.fox")) // <fn>
add(10,10) // 20
```

## Library reference