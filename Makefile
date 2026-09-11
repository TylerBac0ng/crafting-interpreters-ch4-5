JAVAC := javac
JAVA := java
JAVA_SOURCES := $(wildcard src/java/com/craftinginterpreters/lox/*.java)

ifeq ($(OS),Windows_NT)
MKDIR = if not exist "$(subst /,\,$1)" mkdir "$(subst /,\,$1)"
RMDIR = if exist "$(subst /,\,$1)" rmdir /s /q "$(subst /,\,$1)"
else
MKDIR = mkdir -p "$1"
RMDIR = rm -rf "$1"
endif

.PHONY: all test racket-test clean

all: build/classes/com/craftinginterpreters/lox/RunTests.class

build/classes/com/craftinginterpreters/lox/RunTests.class: $(JAVA_SOURCES)
	$(call MKDIR,build/classes)
	$(JAVAC) -g -d build/classes $(JAVA_SOURCES)

test: all
	$(JAVA) -ea -cp build/classes com.craftinginterpreters.lox.RunTests

racket-test:
	raco test src/racket/functional_nodes.rkt

clean:
	$(call RMDIR,build)
