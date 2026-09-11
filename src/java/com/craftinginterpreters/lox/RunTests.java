package com.craftinginterpreters.lox;

import java.util.List;

/** Dependency-free tests for the Chapter 4 and Chapter 5 code challenges. */
public final class RunTests {
  private RunTests() {}

  public static void main(String[] args) {
    testNestedBlockCommentsAndLines();
    testUnterminatedBlockComment();
    testDivisionStillScans();
    testRpnExample();
    testRpnUnaryIsUnambiguous();
    System.out.println("All Chapter 4-5 Java tests passed.");
  }

  private static void testNestedBlockCommentsAndLines() {
    String source = "var before = 1; /* outer\n" +
        "  /* nested\n" +
        "     comment */ still outer */\n" +
        "print before;";
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    expect(scanner.errors().isEmpty(), "valid nested comment produced an error");
    expectTypes(tokens,
        TokenType.VAR, TokenType.IDENTIFIER, TokenType.EQUAL,
        TokenType.NUMBER, TokenType.SEMICOLON,
        TokenType.PRINT, TokenType.IDENTIFIER, TokenType.SEMICOLON,
        TokenType.EOF);
    expect(tokens.get(5).line == 4,
        "newlines inside block comments must advance the token line");
  }

  private static void testUnterminatedBlockComment() {
    Scanner scanner = new Scanner("print 1; /* never closes\n");
    List<Token> tokens = scanner.scanTokens();

    expect(scanner.errors().size() == 1, "unterminated comment needs one error");
    expect(scanner.errors().get(0).contains("Unterminated block comment"),
        "unterminated comment error has the wrong message");
    expect(tokens.get(tokens.size() - 1).type == TokenType.EOF,
        "scanner must still emit EOF after an unterminated comment");
    expect(tokens.get(tokens.size() - 1).line == 2,
        "unterminated comment must still count its newline");
  }

  private static void testDivisionStillScans() {
    Scanner scanner = new Scanner("8 / 2;");
    List<Token> tokens = scanner.scanTokens();
    expectTypes(tokens, TokenType.NUMBER, TokenType.SLASH, TokenType.NUMBER,
        TokenType.SEMICOLON, TokenType.EOF);
  }

  private static void testRpnExample() {
    // (1 + 2) * (4 - 3) -> 1 2 + 4 3 - *
    Expr expression = new Expr.Binary(
        new Expr.Grouping(new Expr.Binary(
            new Expr.Literal(1.0), token(TokenType.PLUS, "+"),
            new Expr.Literal(2.0))),
        token(TokenType.STAR, "*"),
        new Expr.Grouping(new Expr.Binary(
            new Expr.Literal(4.0), token(TokenType.MINUS, "-"),
            new Expr.Literal(3.0))));

    expectEquals("1 2 + 4 3 - *", new RpnPrinter().print(expression));
  }

  private static void testRpnUnaryIsUnambiguous() {
    Expr expression = new Expr.Binary(
        new Expr.Unary(token(TokenType.MINUS, "-"), new Expr.Literal(123.0)),
        token(TokenType.STAR, "*"),
        new Expr.Grouping(new Expr.Literal("str")));
    expectEquals("123 ~ str *", new RpnPrinter().print(expression));
  }

  private static Token token(TokenType type, String lexeme) {
    return new Token(type, lexeme, null, 1);
  }

  private static void expectTypes(List<Token> tokens, TokenType... expected) {
    expect(tokens.size() == expected.length,
        "expected " + expected.length + " tokens, got " + tokens.size());
    for (int index = 0; index < expected.length; index++) {
      expect(tokens.get(index).type == expected[index],
          "token " + index + ": expected " + expected[index] +
              ", got " + tokens.get(index).type);
    }
  }

  private static void expectEquals(String expected, String actual) {
    expect(expected.equals(actual),
        "expected '" + expected + "', got '" + actual + "'");
  }

  private static void expect(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
