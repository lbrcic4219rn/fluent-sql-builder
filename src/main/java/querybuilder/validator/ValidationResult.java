package querybuilder.validator;

import querybuilder.parser.Statement;

import java.util.List;

public record ValidationResult(List<Statement> statements, List<String> errors) {

    public ValidationResult {
        statements = List.copyOf(statements);
        errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
