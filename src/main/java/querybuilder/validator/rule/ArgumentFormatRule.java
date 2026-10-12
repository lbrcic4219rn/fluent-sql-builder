package querybuilder.validator.rule;

import querybuilder.parser.Argument;
import querybuilder.parser.ArgumentKind;
import querybuilder.parser.Call;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ArgumentFormatRule implements IRule {

    private static final String NAME = "[A-Za-z_]\\w*";
    private static final Pattern SIMPLE_NAME = Pattern.compile(NAME);
    private static final Pattern QUALIFIED_NAME = Pattern.compile(NAME + "(\\." + NAME + "){0,2}");
    private static final Set<String> OPERATORS =
        Set.of("=", "<", ">", "<=", ">=", "<>", "!=", "LIKE");

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        for (Call call : statement.calls()) {
            if (!call.hasValidArity()) {
                continue;
            }
            for (int i = 0; i < call.arguments().size(); i++) {
                Argument argument = call.arguments().get(i);
                String problem = problemWith(argument, call.type().kindAt(i));
                if (problem != null) {
                    errors.add(
                        problem
                            + ": "
                            + argument.text()
                            + " for function: "
                            + call.type().keyword());
                }
            }
        }
        return errors;
    }

    private String problemWith(Argument argument, ArgumentKind kind) {
        String text = argument.text();
        return switch (kind) {
            case SELECTABLE -> text.equals("*") || isQualifiedName(text) ? null : "Invalid column";
            case COLUMN -> isQualifiedName(text) ? null : "Invalid column";
            case TABLE -> isQualifiedName(text) ? null : "Invalid table";
            case ALIAS -> isSimpleName(text) ? null : "Invalid alias";
            case QUERY_NAME -> isSimpleName(text) ? null : "Invalid query name";
            case OPERATOR -> OPERATORS.contains(text.toUpperCase(Locale.ROOT))
                ? null
                : "Unsupported operator";
            case VALUE, DATE_LITERAL -> null;
        };
    }

    private boolean isSimpleName(String text) {
        return SIMPLE_NAME.matcher(text).matches();
    }

    private boolean isQualifiedName(String text) {
        return QUALIFIED_NAME.matcher(text).matches();
    }
}
