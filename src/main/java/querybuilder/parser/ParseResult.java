package querybuilder.parser;

import java.util.List;

public record ParseResult(List<Statement> statements, List<String> errors) {

    public ParseResult {
        statements = List.copyOf(statements);
        errors = List.copyOf(errors);
    }
}
