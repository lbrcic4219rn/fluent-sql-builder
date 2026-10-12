package querybuilder.validator.rule;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AggregationAliasMustExistIfHaving implements IRule {

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        if (!statement.hasAny(FunctionType::isHaving)) {
            return errors;
        }
        for (Call aggregation : statement.callsMatching(FunctionType::isAggregation)) {
            if (aggregation.arguments().size() < 2) {
                errors.add(
                    "When using having aggregation function("
                        + aggregation.type().keyword()
                        + ") must contain alias in query: "
                        + statement.name());
            }
        }
        return errors;
    }
}
