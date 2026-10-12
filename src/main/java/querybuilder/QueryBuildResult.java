package querybuilder;

import querybuilder.compiler.CompiledQuery;

import java.util.List;

public record QueryBuildResult(
    CompiledQuery query, List<String> validationErrors, String compileError) {

    public QueryBuildResult {
        validationErrors = List.copyOf(validationErrors);
    }

    public static QueryBuildResult compiled(CompiledQuery query) {
        return new QueryBuildResult(query, List.of(), null);
    }

    public static QueryBuildResult invalid(List<String> validationErrors) {
        return new QueryBuildResult(null, validationErrors, null);
    }

    public static QueryBuildResult failed(String compileError) {
        return new QueryBuildResult(null, List.of(), compileError);
    }

    public boolean isSuccess() {
        return query != null;
    }
}
