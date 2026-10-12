package app;

import database.DataAccessException;
import database.DatabaseConfig;
import database.PostgresRepository;
import database.Repository;

import querybuilder.IQueryBuilder;
import querybuilder.QueryBuildResult;
import querybuilder.QueryBuilder;
import querybuilder.compiler.Compiler;
import querybuilder.validator.Validator;

public class AppCore {

    private final Repository repository;
    private final IQueryBuilder queryBuilder;

    public AppCore(DatabaseConfig config) {
        this(new PostgresRepository(config), new QueryBuilder(new Validator(), new Compiler()));
    }

    public AppCore(Repository repository, IQueryBuilder queryBuilder) {
        this.repository = repository;
        this.queryBuilder = queryBuilder;
    }

    public QueryOutcome runQuery(String source) {
        QueryBuildResult built = queryBuilder.build(source);

        if (!built.validationErrors().isEmpty()) {
            return new QueryOutcome.Invalid(built.validationErrors());
        }
        if (!built.isSuccess()) {
            return new QueryOutcome.Failed(built.compileError());
        }

        try {
            return new QueryOutcome.Success(
                repository.query(built.query().sql(), built.query().parameters()));
        } catch (DataAccessException e) {
            return new QueryOutcome.Failed(e.getMessage());
        }
    }

    public void shutdown() {
        repository.close();
    }
}
