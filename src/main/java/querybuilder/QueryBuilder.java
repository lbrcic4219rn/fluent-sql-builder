package querybuilder;

import querybuilder.compiler.CompilationResult;
import querybuilder.compiler.ICompiler;
import querybuilder.validator.IValidator;
import querybuilder.validator.ValidationResult;

public class QueryBuilder implements IQueryBuilder {

    private final IValidator validator;
    private final ICompiler compiler;

    public QueryBuilder(IValidator validator, ICompiler compiler) {
        this.validator = validator;
        this.compiler = compiler;
    }

    @Override
    public QueryBuildResult build(String source) {
        ValidationResult validation = validator.validate(source);
        if (!validation.isValid()) {
            return QueryBuildResult.invalid(validation.errors());
        }
        CompilationResult compilation = compiler.compile(validation.statements());
        if (!compilation.isSuccess()) {
            return QueryBuildResult.failed(compilation.error());
        }
        return QueryBuildResult.compiled(compilation.query());
    }
}
