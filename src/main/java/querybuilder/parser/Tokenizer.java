package querybuilder.parser;

import java.util.ArrayList;
import java.util.List;

final class Tokenizer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int position;

    Tokenizer(String source) {
        this.source = source;
    }

    List<Token> tokenize() {
        while (position < source.length()) {
            char current = source.charAt(position);
            if (Character.isWhitespace(current)) {
                position++;
            } else if (current == '"') {
                readString();
            } else if (startsNumber()) {
                readNumber();
            } else if (isWordChar(current)) {
                readWord();
            } else {
                tokens.add(new Token(symbolType(current), String.valueOf(current)));
                position++;
            }
        }
        return tokens;
    }

    private Token.Type symbolType(char symbol) {
        return switch (symbol) {
            case '.' -> Token.Type.DOT;
            case ',' -> Token.Type.COMMA;
            case '(' -> Token.Type.OPEN_PARENTHESIS;
            case ')' -> Token.Type.CLOSE_PARENTHESIS;
            case '=' -> Token.Type.EQUALS;
            default -> throw new SyntaxException(
                "Unexpected character '" + symbol + "' at position " + (position + 1));
        };
    }

    private void readString() {
        int start = position;
        StringBuilder text = new StringBuilder();
        position++;
        while (position < source.length()) {
            char current = source.charAt(position++);
            if (current == '"') {
                tokens.add(new Token(Token.Type.STRING, text.toString()));
                return;
            }
            if (current == '\\' && position < source.length() && isEscapable(peek())) {
                current = source.charAt(position++);
            }
            text.append(current);
        }
        throw new SyntaxException(
            "Missing closing quote for string starting at position " + (start + 1));
    }

    private boolean isEscapable(char candidate) {
        return candidate == '"' || candidate == '\\';
    }

    private boolean startsNumber() {
        char current = peek();
        if (current == '-') {
            return position + 1 < source.length() && Character.isDigit(source.charAt(position + 1));
        }
        if (!Character.isDigit(current)) {
            return false;
        }
        int end = position;
        while (end < source.length() && Character.isDigit(source.charAt(end))) {
            end++;
        }
        return end == source.length() || !isWordChar(source.charAt(end));
    }

    private void readNumber() {
        int start = position;
        position++;
        skipDigits();
        boolean hasFraction =
            position + 1 < source.length()
                && peek() == '.'
                && Character.isDigit(source.charAt(position + 1));
        if (hasFraction) {
            position++;
            skipDigits();
        }
        tokens.add(new Token(Token.Type.NUMBER, source.substring(start, position)));
    }

    private void skipDigits() {
        while (position < source.length() && Character.isDigit(peek())) {
            position++;
        }
    }

    private void readWord() {
        int start = position;
        while (position < source.length() && isWordChar(peek())) {
            position++;
        }
        tokens.add(new Token(Token.Type.WORD, source.substring(start, position)));
    }

    private boolean isWordChar(char candidate) {
        return Character.isLetterOrDigit(candidate) || candidate == '_';
    }

    private char peek() {
        return source.charAt(position);
    }
}
