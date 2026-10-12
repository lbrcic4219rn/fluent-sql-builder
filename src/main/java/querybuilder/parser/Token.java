package querybuilder.parser;

record Token(Type type, String text) {

    boolean is(Type expected) {
        return type == expected;
    }

    boolean isWord(String expected) {
        return type == Type.WORD && text.equals(expected);
    }

    enum Type {
        WORD,
        NUMBER,
        STRING,
        DOT,
        COMMA,
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        EQUALS
    }
}
