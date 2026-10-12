package querybuilder.validator.rule;

import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConditionChainRule implements IRule {

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        if (statement.hasAny(FunctionType::continuesWhere)
            && !statement.hasAny(FunctionType::startsWhere)) {
            errors.add(
                "Cant use AndWhere and or OrWhere if where is not declared query: "
                    + statement.name());
        }
        if (statement.hasAny(FunctionType::continuesHaving)
            && statement.callsOf(FunctionType.HAVING).isEmpty()) {
            errors.add(
                "Cant use AndHaving and or OrHaving if having is not declared query: "
                    + statement.name());
        }
        return errors;
    }
}
