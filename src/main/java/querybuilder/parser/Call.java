package querybuilder.parser;

import java.util.List;

public record Call(FunctionType type, List<Argument> arguments) {

    public Call {
        arguments = List.copyOf(arguments);
    }

    public String text(int index) {
        return arguments.get(index).text();
    }

    public List<String> texts() {
        return arguments.stream().map(Argument::text).toList();
    }

    public boolean hasValidArity() {
        return type.accepts(arguments.size());
    }
}
