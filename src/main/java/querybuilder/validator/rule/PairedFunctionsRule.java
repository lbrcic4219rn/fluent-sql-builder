package querybuilder.validator.rule;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class PairedFunctionsRule implements IRule {

    private final FunctionType leader;
    private final FunctionType follower;

    protected PairedFunctionsRule(FunctionType leader, FunctionType follower) {
        this.leader = leader;
        this.follower = follower;
    }

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        List<Call> calls = statement.calls();
        for (int i = 0; i < calls.size(); i++) {
            FunctionType current = calls.get(i).type();
            if (current == leader && !isAt(calls, i + 1, follower)) {
                errors.add(
                    "When using "
                        + leader.keyword()
                        + " function "
                        + follower.keyword()
                        + " must be used right after it in query: "
                        + statement.name());
            }
            if (current == follower && !isAt(calls, i - 1, leader)) {
                errors.add(
                    follower.keyword()
                        + " can only be used right after "
                        + leader.keyword()
                        + " function in query: "
                        + statement.name());
            }
        }
        return errors;
    }

    private boolean isAt(List<Call> calls, int index, FunctionType expected) {
        return index >= 0 && index < calls.size() && calls.get(index).type() == expected;
    }
}
