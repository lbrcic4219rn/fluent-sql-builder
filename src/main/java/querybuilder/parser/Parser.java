package querybuilder.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Parser {

    private static final String VAR = "var";
    private static final String NEW = "new";

    public ParseResult parse(String source) {
        List<Token> tokens;
        try {
            tokens = new Tokenizer(source).tokenize();
        } catch (SyntaxException e) {
            return new ParseResult(List.of(), List.of(e.getMessage()));
        }

        if (tokens.stream().noneMatch(token -> token.isWord(VAR))) {
            return new ParseResult(List.of(), List.of("No variables declared"));
        }
        if (!tokens.getFirst().isWord(VAR)) {
            return new ParseResult(List.of(), List.of("First query missing var"));
        }

        List<Statement> statements = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> names = new HashSet<>();
        int statementNo = 0;

        for (List<Token> statementTokens : splitStatements(tokens)) {
            statementNo++;
            try {
                Statement statement = new StatementParser(statementTokens, statementNo).parse();
                if (names.add(statement.name())) {
                    statements.add(statement);
                } else {
                    errors.add("Variables can not have the same name: " + statement.name());
                }
            } catch (SyntaxException e) {
                errors.add(e.getMessage());
            }
        }
        return new ParseResult(statements, errors);
    }

    private List<List<Token>> splitStatements(List<Token> tokens) {
        List<List<Token>> statements = new ArrayList<>();
        List<Token> current = null;
        for (Token token : tokens) {
            if (token.isWord(VAR)) {
                current = new ArrayList<>();
                statements.add(current);
            } else if (current != null) {
                current.add(token);
            }
        }
        return statements;
    }

    private static final class StatementParser {

        private final List<Token> tokens;
        private final int statementNo;
        private int position;

        StatementParser(List<Token> tokens, int statementNo) {
            this.tokens = tokens;
            this.statementNo = statementNo;
        }

        Statement parse() {
            String name = parseName();
            if (atEnd() || !next().is(Token.Type.EQUALS)) {
                throw new SyntaxException("Missing = after variable name: " + name);
            }
            if (atEnd() || !next().isWord(NEW)) {
                throw new SyntaxException("Missing keyword new in query: " + name);
            }

            List<Call> calls = new ArrayList<>();
            do {
                calls.add(parseCall(name, calls.isEmpty()));
            } while (hasNextCall(calls.getLast()));
            return new Statement(name, calls);
        }

        private String parseName() {
            if (atEnd() || peek().is(Token.Type.EQUALS)) {
                throw new SyntaxException(
                    "Var name not specified for statement no: " + statementNo);
            }
            Token name = next();
            if (!name.is(Token.Type.WORD) || !Character.isLetter(name.text().charAt(0))) {
                throw new SyntaxException(
                    "Variable name cant start with special characters or numbers statement"
                        + " no: "
                        + statementNo);
            }
            return name.text();
        }

        private boolean hasNextCall(Call previous) {
            if (atEnd()) {
                return false;
            }
            if (!next().is(Token.Type.DOT)) {
                throw new SyntaxException(
                    "Missing . operator after function: " + previous.type().keyword());
            }
            return true;
        }

        private Call parseCall(String statementName, boolean first) {
            if (atEnd()) {
                throw new SyntaxException("Missing function name in query: " + statementName);
            }
            Token nameToken = next();
            String name = nameToken.text();
            FunctionType type =
                FunctionType.fromKeyword(name)
                    .filter(candidate -> nameToken.is(Token.Type.WORD))
                    .orElseThrow(() -> new SyntaxException("Unknown function: " + name));

            if (first && type != FunctionType.QUERY) {
                throw new SyntaxException(
                    "Query must start with new Query(...) in query: " + statementName);
            }
            if (!first && type == FunctionType.QUERY) {
                throw new SyntaxException(
                    "Query can only be used once, after new, in query: " + statementName);
            }
            if (atEnd() || !next().is(Token.Type.OPEN_PARENTHESIS)) {
                throw new SyntaxException("Missing ( for function: " + name);
            }
            return new Call(type, parseArguments(name));
        }

        private List<Argument> parseArguments(String function) {
            List<Argument> arguments = new ArrayList<>();
            if (!atEnd() && peek().is(Token.Type.CLOSE_PARENTHESIS)) {
                position++;
                return arguments;
            }
            while (true) {
                arguments.add(parseArgument(function));
                if (atEnd()) {
                    throw new SyntaxException("Missing ) for function: " + function);
                }
                Token separator = next();
                if (separator.is(Token.Type.CLOSE_PARENTHESIS)) {
                    return arguments;
                }
                if (!separator.is(Token.Type.COMMA)) {
                    throw new SyntaxException(
                        "Problem with quotes and or , operator for function: " + function);
                }
            }
        }

        private Argument parseArgument(String function) {
            if (atEnd()) {
                throw new SyntaxException("Missing ) for function: " + function);
            }
            Token token = next();
            return switch (token.type()) {
                case STRING -> new Argument(token.text(), true);
                case NUMBER -> new Argument(token.text(), false);
                case WORD -> throw new SyntaxException(
                    "Text arguments need to be surrounded by quotes: "
                        + token.text()
                        + " for function: "
                        + function);
                default -> throw new SyntaxException("Missing argument for function: " + function);
            };
        }

        private boolean atEnd() {
            return position >= tokens.size();
        }

        private Token peek() {
            return tokens.get(position);
        }

        private Token next() {
            return tokens.get(position++);
        }
    }
}
