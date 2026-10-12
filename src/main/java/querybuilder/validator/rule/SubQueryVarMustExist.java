package querybuilder.validator.rule;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubQueryVarMustExist implements IRule {

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        for (Call call : statement.callsMatching(FunctionType::isSubQuery)) {
            String needed = call.text(1);
            if (!declaredQueries.contains(needed)) {
                errors.add(
                    "Query: " + needed + " must be declared before query: " + statement.name());
            }
        }
        return errors;
    }
}
