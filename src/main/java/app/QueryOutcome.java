package app;

import database.QueryResult;

import java.util.List;

public sealed interface QueryOutcome {

    record Success(QueryResult result) implements QueryOutcome {
    }

    record Invalid(List<String> errors) implements QueryOutcome {
    }

    record Failed(String message) implements QueryOutcome {
    }
}
