package querybuilder.parser;

import java.util.List;
import java.util.function.Predicate;

public record Statement(String name, List<Call> calls) {

    public Statement {
        calls = List.copyOf(calls);
    }

    public List<Call> callsOf(FunctionType type) {
        return callsMatching(candidate -> candidate == type);
    }

    public List<Call> callsMatching(Predicate<FunctionType> predicate) {
        return calls.stream().filter(call -> predicate.test(call.type())).toList();
    }

    public boolean hasAny(Predicate<FunctionType> predicate) {
        return calls.stream().anyMatch(call -> predicate.test(call.type()));
    }
}
