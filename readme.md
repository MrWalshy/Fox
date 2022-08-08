# Fox

Idea: A language based on function expressions. Everything in the language is either a function expression or some atomic value in the language.

- Some built-in statements and operations will exist for extending the languages functionality.

## Spec

An example of the language:

```
// define a function
defun(h1, (text) ->
  ("<h1>" + text + "</h1>")
)

// shortcut
var capturedH2 = defun(h2, (text) -> "<h2>" + text + "</h2>")

var(title, h1("Hello World"));

if((true), print(title), print(h2("Subtitle")))
```

As everything is an expression, everything will implicitly return a value. The last line in a function returns its value for example.

### Grammar

#### Primary

These concern the core primitive data in the language that can be passed around like a variable or result in a value when evaluated:

```
primary          -> "true" | "false" | "null"
                  | NUMBER | STRING
                  | "(" expression ")"
                  | IDENTIFIER
                  | functionDefiner ;

functionDefiner  -> "defun" "(" IDENTIFIER "," function ")" ;
function         -> "(" parameters? ")" "->" expression ;
parameters       -> IDENTIFIER ( "," IDENTIFIER )* ;
```

The function definer is used to define the languages functionality. Core types are similar to Lox to help keep it similar.

#### Function calling

Same as Lox:

```
call             -> primary ( "(" arguments? ")" )+ ;
arguments        -> expression ( "," expression )* ;
```

- may be multiple calls in terms of a closure, always at least one call is made
- arguments are optional, many may be specified

#### Other stuff is the same as Lox really

```
program                 -> expression* EOF ;

expression              -> ternaryExpression 
                         | expressionBlock 
                         | functionDefiner
                         | varDefiner
                         | varAssignment ;
                         
functionDefiner         -> "defun" "(" STRING "," function ")" ;
function                -> "(" parameters? ")" "->" ( expressionBlock | ternaryExpression | function );
parameters              -> IDENTIFIER ( "," IDENTIFIER )* ;

varDefiner              -> "var" "(" STRING "," ( expressionBlock | ternaryExpression ) ")" ;

varAssignment           -> "assign" "(" IDENTIFIER "," ( expressionBlock | ternaryExpression ) ")" ;

expressionBlock         -> "{" expression ( "," expression )* "}" ;

ternaryExpression       -> logicOr ( "?" expression : expression )? ;
logicOr                 -> logicAnd ( "or" logicAnd )* ;
logicAnd                -> equality ( "and" equality )* ;
equality                -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison              -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term                    -> factor ( ( "-" | "+" ) factor )* ;
factor                  -> unary ( ( "/" | "*" ) unary )* ;
unary                   -> ( "!" | "-" ) unary | call ;

call                    -> primary ( "(" arguments? ")" )+ ;
arguments               -> expression ( "," expression )* ;

primary                 -> "true" | "false" | "null"
                         | NUMBER | STRING
                         | "(" expression ")"
                         | IDENTIFIER 
                         | "defun" "(" function ")" ;

```
