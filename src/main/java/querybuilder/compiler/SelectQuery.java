package querybuilder.compiler;

import java.util.ArrayList;
import java.util.List;

final class SelectQuery {

    private final List<String> columns = new ArrayList<>();
    private final List<String> aggregations = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private final ConditionList where = new ConditionList();
    private final List<String> groupBy = new ArrayList<>();
    private final ConditionList having = new ConditionList();
    private final List<String> orderBy = new ArrayList<>();
    private String table;

    void from(String tableName) {
        this.table = tableName;
    }

    void select(List<String> columnNames) {
        columns.addAll(columnNames);
    }

    void aggregate(String expression) {
        aggregations.add(expression);
    }

    void join(String joinedTable, String condition) {
        joins.add("JOIN " + joinedTable + " ON " + condition);
    }

    ConditionList where() {
        return where;
    }

    void groupBy(List<String> columnNames) {
        groupBy.addAll(columnNames);
    }

    ConditionList having() {
        return having;
    }

    void orderBy(String term) {
        orderBy.add(term);
    }

    CompiledQuery build() {
        if (table == null) {
            throw new CompilationException("Query is missing a table");
        }

        List<String> selected = new ArrayList<>(columns);
        selected.addAll(aggregations);

        List<String> clauses = new ArrayList<>();
        clauses.add("SELECT " + (selected.isEmpty() ? "*" : String.join(", ", selected)));
        clauses.add("FROM " + table);
        clauses.addAll(joins);
        if (!where.isEmpty()) {
            clauses.add("WHERE " + where.sql());
        }
        if (!groupBy.isEmpty()) {
            clauses.add("GROUP BY " + String.join(", ", groupBy));
        }
        if (!having.isEmpty()) {
            clauses.add("HAVING " + having.sql());
        }
        if (!orderBy.isEmpty()) {
            clauses.add("ORDER BY " + String.join(", ", orderBy));
        }

        List<String> parameters = new ArrayList<>(where.parameters());
        parameters.addAll(having.parameters());
        return new CompiledQuery(String.join(" ", clauses), parameters);
    }
}
