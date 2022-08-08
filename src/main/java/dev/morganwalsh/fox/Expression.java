package dev.morganwalsh.fox;

import java.util.List;

public abstract class Expression {
  interface Visitor<R> {
    R visitVarExpression(Var expression);
    R visitBlockExpression(Block expression);
    R visitTernaryExpression(Ternary expression);
    R visitBinaryExpression(Binary expression);
    R visitCallExpression(Call expression);
    R visitGroupingExpression(Grouping expression);
    R visitLiteralExpression(Literal expression);
    R visitLogicalExpression(Logical expression);
    R visitUnaryExpression(Unary expression);
    R visitFunctionExpression(Function expression);
    R visitVariableExpression(Variable expression);
    R visitAssignExpression(Assign expression);
    R visitImportExpression(Import expression);
  }
  static class Var extends Expression {
    Var(Token name, Expression initialiser) {
      this.name = name;
      this.initialiser = initialiser;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarExpression(this);
    }

    final Token name;
    final Expression initialiser;
  }
  static class Block extends Expression {
    Block(List<Expression> expressions) {
      this.expressions = expressions;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockExpression(this);
    }

    final List<Expression> expressions;
  }
  static class Ternary extends Expression {
    Ternary(Expression condition, Expression ifTrue, Expression ifFalse, Token operator) {
      this.condition = condition;
      this.ifTrue = ifTrue;
      this.ifFalse = ifFalse;
      this.operator = operator;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpression(this);
    }

    final Expression condition;
    final Expression ifTrue;
    final Expression ifFalse;
    final Token operator;
  }
  static class Binary extends Expression {
    Binary(Expression left, Token operator, Expression right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpression(this);
    }

    final Expression left;
    final Token operator;
    final Expression right;
  }
  static class Call extends Expression {
    Call(Expression callee, Token closingParenthesis, List<Expression> arguments) {
      this.callee = callee;
      this.closingParenthesis = closingParenthesis;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpression(this);
    }

    final Expression callee;
    final Token closingParenthesis;
    final List<Expression> arguments;
  }
  static class Grouping extends Expression {
    Grouping(Expression expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpression(this);
    }

    final Expression expression;
  }
  static class Literal extends Expression {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpression(this);
    }

    final Object value;
  }
  static class Logical extends Expression {
    Logical(Expression left, Token operator, Expression right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpression(this);
    }

    final Expression left;
    final Token operator;
    final Expression right;
  }
  static class Unary extends Expression {
    Unary(Token operator, Expression right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpression(this);
    }

    final Token operator;
    final Expression right;
  }
  static class Function extends Expression {
    Function(Token identifier, List<Token> params, Expression body) {
      this.identifier = identifier;
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionExpression(this);
    }

    final Token identifier;
    final List<Token> params;
    final Expression body;
  }
  static class Variable extends Expression {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpression(this);
    }

    final Token name;
  }
  static class Assign extends Expression {
    Assign(Token name, Expression assignment) {
      this.name = name;
      this.assignment = assignment;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpression(this);
    }

    final Token name;
    final Expression assignment;
  }
  static class Import extends Expression {
    Import(Token file) {
      this.file = file;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitImportExpression(this);
    }

    final Token file;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
