package querybuilder.validator.rule;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArgNumberRule implements IRule {

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        for (Call call : statement.calls()) {
            if (call.hasValidArity()) {
                continue;
            }
            FunctionType type = call.type();
            int actual = call.arguments().size();
            if (actual == 0) {
                errors.add("Function can not have zero arguments for function: " + type.keyword());
            } else {
                errors.add(
                    "Expected "
                        + expected(type)
                        + " arguments but got: "
                        + actual
                        + " for function: "
                        + type.keyword());
            }
        }
        return errors;
    }

    private String expected(FunctionType type) {
        if (type.minArguments() == type.maxArguments()) {
            return String.valueOf(type.minArguments());
        }
        return type.minArguments() + " to " + type.maxArguments();
    }
}
