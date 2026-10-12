package querybuilder.validator.rule;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnlyAggregationElementsInHavingRule implements IRule {

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        List<Call> havings = statement.callsMatching(FunctionType::isHaving);
        if (havings.isEmpty()) {
            return errors;
        }

        List<Call> aggregations = statement.callsMatching(FunctionType::isAggregation);
        if (aggregations.isEmpty()) {
            errors.add(
                "No valid aggregation arguments/functions found but has having (having can only"
                    + " use aggregation function alias) query: "
                    + statement.name());
            return errors;
        }

        Set<String> aliases = new HashSet<>();
        for (Call aggregation : aggregations) {
            if (aggregation.arguments().size() == 2) {
                aliases.add(aggregation.text(1));
            }
        }
        for (Call having : havings) {
            if (!aliases.contains(having.text(0))) {
                errors.add(
                    "When using having andhaving orhaving only aggregation aliases can be used,"
                        + " found: "
                        + having.text(0)
                        + " in query: "
                        + statement.name());
            }
        }
        return errors;
    }
}
