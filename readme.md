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

- array

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

Strings can also be concatenated together to form a new string:

```
"Hello " + "World"
```

##### Escape sequences

| Sequence | Description |
| -------- | ----------- |
| `\t`     | Inserts a tab |
| `\n`     | Newline character |
| `\\`     | Insert a backslash into the text |
| `\"`     | Escapes a quote |
| `\b`     | Backspace |
| `\r`     | Carriage return |
| `\f`     | Form feed |

#### boolean

A boolean is a truthy or falsey value. In Fox, `null` and `\0` are considered falsey, everything else is truthy.

Boolean values are represented using `true` or `false`.

#### Arrays

Arrays in Fox are static in size, but their contents are mutable. When an array is created, it will be set to the size of the number of elements passed to it:

```
var(myArr, [1,2,3])
```

Once an array has been created, values can be accessed using bracket notation with a supplied index:

```
myArr[0] // 1
myArr[2] // 3
```

Arrays start counting from 0.

To get the length of an array, use the built-in `len` function:

```
var(length, len(myArr)) // 3
```

Arrays may be fixed in size, but they are mutable through use of the built-in `set` function:

```
set(myArr, 0, 22)
```

- The `set` function accepts an array to modify, followed by an index and then the value to insert

Arrays in Fox are not homogeneous.... Instead, they are heterogeneous, allowing values of different types to be stored in them.

```
var(myVariedArr, [1, "Hello", true, null])
```

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

### `match` expression

The `match` expression is an alternative to ternary statements for conditional logic, a `match` expression accepts some value to compare cases against:

```
match value {
  value1 => doSomething()
  value2 => doSomethingElse()
  _      => defaultCase()
}
```

A realistic example might look like:

```
var(weather, "sunny")

match weather {
  "sunny" => print("Wear suncream")
  _       => print("Pack a brolly")
}
```

This would print `Wear suncream`, it would also return that value as the built-in `print` function returns the value it prints.

- The default case is optional

An interesting use case for match expressions is for input, for example:

```
var(select, '(in) -> match in {
  "Play" => "3 + 3"
  "Exit" => "\"Good\\\"bye\""
  _      => "\"Invalid input\""
})

print(eval(select(input(">"))))
```

This looks very much like a REPL, just without the L :D

- the `eval` has been thrown in here just to show that you can evaluate code that is in a string in the enclosing scope.

This program demonstates a variable called `select` that has been bound to an anonymous function. The anonymous function accepts one parameter representing the input and has a match expression for its body, the value from the matching case is returned.

#### Enhanced matching

A `match` expressions cases can also be specified within parenthesis, this will transform the case to an enhanced case expression.

Enhanced case expressions have alternate behaviours when matching, the simplest being the pipe symbol for matching against any of the specified values:

```
var(in, input("Username: "))

match in {
  ( "Bob" | "Fred" ) => print("Not another Bob or Fred")
  _ => print("Username length is " + len(in))
}
```

An interesting thing that can be done with match expressions is matching against some string input from the user:

```
// generate random number, store as string for comparison
var(x, "50")
print("Please guess a number\n")

// matcher
match x {
  input("> ") => print("Correct")
  _ => print("Incorrect")
}
```

Just because it is possible, doesn't mean it is the best way of performing that task. The following code illustrates the same number guesser using a ternary expression:

```
var(x, "50")
print("Please guess a number\n")

input("> ") == x ? print("Correct") : print("Incorrect") 
```

## Library reference