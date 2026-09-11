# Crafting Interpreters — Chapters 4–5 Challenges

This directory contains the completed challenges for Chapter 4, “Scanning,”
and Chapter 5, “Representing Code.”

## Run the tests

```powershell
mingw32-make test
mingw32-make racket-test
```

The Java tests cover nested block comments, line tracking, unterminated block
comments, division tokens, and reverse Polish notation. The Racket tests cover
the functional complement to the Visitor pattern.

The written report and submission PDF are in [`submission`](submission/).
