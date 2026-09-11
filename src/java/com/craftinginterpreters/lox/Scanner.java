package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lox scanner extended with newline-aware, nested C-style block comments. */
final class Scanner {
  private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
      Map.entry("and", TokenType.AND),
      Map.entry("class", TokenType.CLASS),
      Map.entry("else", TokenType.ELSE),
      Map.entry("false", TokenType.FALSE),
      Map.entry("for", TokenType.FOR),
      Map.entry("fun", TokenType.FUN),
      Map.entry("if", TokenType.IF),
      Map.entry("nil", TokenType.NIL),
      Map.entry("or", TokenType.OR),
      Map.entry("print", TokenType.PRINT),
      Map.entry("return", TokenType.RETURN),
      Map.entry("super", TokenType.SUPER),
      Map.entry("this", TokenType.THIS),
      Map.entry("true", TokenType.TRUE),
      Map.entry("var", TokenType.VAR),
      Map.entry("while", TokenType.WHILE));

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();
  private int start;
  private int current;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return List.copyOf(tokens);
  }

  List<String> errors() {
    return List.copyOf(errors);
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '{' -> addToken(TokenType.LEFT_BRACE);
      case '}' -> addToken(TokenType.RIGHT_BRACE);
      case ',' -> addToken(TokenType.COMMA);
      case '.' -> addToken(TokenType.DOT);
      case '-' -> addToken(TokenType.MINUS);
      case '+' -> addToken(TokenType.PLUS);
      case ';' -> addToken(TokenType.SEMICOLON);
      case '*' -> addToken(TokenType.STAR);
      case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
      case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
      case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
      case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
      case '/' -> {
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else if (match('*')) {
          blockComment();
        } else {
          addToken(TokenType.SLASH);
        }
      }
      case ' ', '\r', '\t' -> {
        // Insignificant whitespace.
      }
      case '\n' -> line++;
      case '"' -> string();
      default -> {
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          errors.add("[line " + line + "] Error: Unexpected character '" + c + "'.");
        }
      }
    }
  }

  /** Consumes a block comment, using a depth counter to support nesting. */
  private void blockComment() {
    int depth = 1;

    while (depth > 0 && !isAtEnd()) {
      if (peek() == '/' && peekNext() == '*') {
        advance();
        advance();
        depth++;
      } else if (peek() == '*' && peekNext() == '/') {
        advance();
        advance();
        depth--;
      } else {
        if (peek() == '\n') {
          line++;
        }
        advance();
      }
    }

    if (depth > 0) {
      errors.add("[line " + line + "] Error: Unterminated block comment.");
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }

    String text = source.substring(start, current);
    addToken(KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER));
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }

    if (peek() == '.' && isDigit(peekNext())) {
      advance();
      while (isDigit(peek())) {
        advance();
      }
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    if (isAtEnd()) {
      errors.add("[line " + line + "] Error: Unterminated string.");
      return;
    }

    advance();
    addToken(TokenType.STRING, source.substring(start + 1, current - 1));
  }

  private boolean match(char expected) {
    if (isAtEnd() || source.charAt(current) != expected) {
      return false;
    }
    current++;
    return true;
  }

  private char peek() {
    return isAtEnd() ? '\0' : source.charAt(current);
  }

  private char peekNext() {
    return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
