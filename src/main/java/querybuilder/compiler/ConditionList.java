package querybuilder.compiler;

import java.util.ArrayList;
import java.util.List;

final class ConditionList {

    private final StringBuilder sql = new StringBuilder();
    private final List<String> parameters = new ArrayList<>();

    void add(String connector, String condition, List<String> conditionParameters) {
        if (!sql.isEmpty()) {
            sql.append(' ').append(connector).append(' ');
        }
        sql.append(condition);
        parameters.addAll(conditionParameters);
    }

    boolean isEmpty() {
        return sql.isEmpty();
    }

    String sql() {
        return sql.toString();
    }

    List<String> parameters() {
        return parameters;
    }
}
