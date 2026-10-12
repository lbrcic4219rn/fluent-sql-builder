package database;

import java.util.List;

public record QueryResult(List<String> columns, List<List<Object>> rows) {

    public QueryResult {
        columns = List.copyOf(columns);
    }

    public static QueryResult empty() {
        return new QueryResult(List.of(), List.of());
    }
}
