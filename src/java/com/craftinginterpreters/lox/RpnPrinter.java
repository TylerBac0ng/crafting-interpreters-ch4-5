package com.craftinginterpreters.lox;

/** Converts a Lox expression syntax tree to reverse Polish notation. */
final class RpnPrinter implements Expr.Visitor<String> {
  String print(Expr expression) {
    return expression.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expression) {
    return expression.left.accept(this) + " " +
        expression.right.accept(this) + " " + expression.operator.lexeme;
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expression) {
    return expression.expression.accept(this);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expression) {
    if (expression.value == null) {
      return "nil";
    }
    if (expression.value instanceof Double number && number == Math.rint(number)) {
      return Long.toString(number.longValue());
    }
    return expression.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expression) {
    // A distinct symbol tells an RPN evaluator to pop one operand, not two.
    String operator = expression.operator.type == TokenType.MINUS
        ? "~"
        : expression.operator.lexeme;
    return expression.right.accept(this) + " " + operator;
  }
}
