package querybuilder.validator.rule;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NonAggregationElementsInGroupByRule implements IRule {

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        if (!statement.hasAny(FunctionType::isAggregation)) {
            return errors;
        }

        List<String> selected = argumentsOf(statement, FunctionType.SELECT);
        if (selected.isEmpty()) {
            return errors;
        }
        if (statement.callsOf(FunctionType.GROUP_BY).isEmpty()) {
            errors.add(
                "GroupBy function is not declared -- Non Aggregation elements must be in"
                    + " GroupBy query: "
                    + statement.name());
            return errors;
        }

        List<String> grouped = argumentsOf(statement, FunctionType.GROUP_BY);
        for (String column : selected) {
            if (!grouped.contains(column)) {
                errors.add(column + " from select must be in groupBy function");
            }
        }
        return errors;
    }

    private List<String> argumentsOf(Statement statement, FunctionType type) {
        List<String> arguments = new ArrayList<>();
        for (Call call : statement.callsOf(type)) {
            arguments.addAll(call.texts());
        }
        return arguments;
    }
}
