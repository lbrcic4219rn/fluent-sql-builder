package querybuilder.compiler;

import java.util.List;

public record CompiledQuery(String sql, List<String> parameters) {

    public CompiledQuery {
        parameters = List.copyOf(parameters);
    }
}
