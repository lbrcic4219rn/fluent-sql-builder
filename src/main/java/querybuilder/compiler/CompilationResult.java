package querybuilder.compiler;

public record CompilationResult(CompiledQuery query, String error) {

    public static CompilationResult success(CompiledQuery query) {
        return new CompilationResult(query, null);
    }

    public static CompilationResult failure(String error) {
        return new CompilationResult(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
