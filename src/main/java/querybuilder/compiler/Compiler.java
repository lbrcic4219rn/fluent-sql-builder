package querybuilder.compiler;

import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.util.*;

public class Compiler implements ICompiler {

    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String NOTHING_TO_COMPILE = "No query to compile";
    private static final Set<FunctionType> APPLIED_BY_OTHER_CALLS =
        EnumSet.of(FunctionType.ON, FunctionType.PARAMETER_LIST, FunctionType.DATE);

    @Override
    public CompilationResult compile(List<Statement> statements) {
        Map<String, CompiledQuery> compiled = new HashMap<>();
        CompiledQuery last = null;

        try {
            for (Statement statement : statements) {
                last = new StatementCompiler(statement, compiled).compile();
                compiled.put(statement.name(), last);
            }
        } catch (CompilationException e) {
            return CompilationResult.failure(e.getMessage());
        }

        if (last == null) {
            return CompilationResult.failure(NOTHING_TO_COMPILE);
        }
        return CompilationResult.success(last);
    }

    private static final class StatementCompiler {

        private final Statement statement;
        private final Map<String, CompiledQuery> compiledQueries;
        private final Map<String, String> aggregationsByAlias = new HashMap<>();
        private final Map<String, String> datesByAlias = new HashMap<>();
        private final SelectQuery query = new SelectQuery();

        StatementCompiler(Statement statement, Map<String, CompiledQuery> compiledQueries) {
            this.statement = statement;
            this.compiledQueries = compiledQueries;
        }

        CompiledQuery compile() {
            for (Call call : statement.callsOf(FunctionType.DATE)) {
                datesByAlias.put(call.text(1), call.text(0));
            }
            for (Call call : statement.callsMatching(FunctionType::isAggregation)) {
                if (call.arguments().size() > 1) {
                    aggregationsByAlias.put(call.text(1), aggregation(call));
                }
            }

            List<Call> calls = statement.calls();
            for (int i = 0; i < calls.size(); i++) {
                apply(calls.get(i), i + 1 < calls.size() ? calls.get(i + 1) : null);
            }
            return query.build();
        }

        private void apply(Call call, Call next) {
            if (APPLIED_BY_OTHER_CALLS.contains(call.type())) {
                return;
            }
            switch (call.type()) {
                case QUERY -> query.from(call.text(0));
                case SELECT -> query.select(call.texts());
                case AVG, COUNT, MIN, MAX -> query.aggregate(aliased(call));
                case JOIN -> join(call, next);
                case WHERE, AND_WHERE -> comparison(query.where(), AND, call.text(0), call);
                case OR_WHERE -> comparison(query.where(), OR, call.text(0), call);
                case WHERE_BETWEEN -> between(call);
                case WHERE_IN -> in(call, next);
                case WHERE_STARTS_WITH -> like(call, escapeLike(call.text(1)) + "%");
                case WHERE_ENDS_WITH -> like(call, "%" + escapeLike(call.text(1)));
                case WHERE_CONTAINS -> like(call, "%" + escapeLike(call.text(1)) + "%");
                case WHERE_IN_Q -> subQuery(call, "IN");
                case WHERE_EQ_Q -> subQuery(call, "=");
                case GROUP_BY -> query.groupBy(call.texts());
                case HAVING, AND_HAVING ->
                    comparison(query.having(), AND, aggregationOf(call), call);
                case OR_HAVING -> comparison(query.having(), OR, aggregationOf(call), call);
                case ORDER_BY -> call.texts().forEach(query::orderBy);
                case ORDER_BY_DESC ->
                    call.texts().forEach(column -> query.orderBy(column + " DESC"));
                default ->
                    throw new CompilationException(
                        "Unsupported function: " + call.type().keyword());
            }
        }

        private String aggregation(Call call) {
            return call.type().keyword().toUpperCase(Locale.ROOT) + "(" + call.text(0) + ")";
        }

        private String aliased(Call call) {
            String expression = aggregation(call);
            return call.arguments().size() > 1 ? expression + " AS " + call.text(1) : expression;
        }

        private String aggregationOf(Call having) {
            String expression = aggregationsByAlias.get(having.text(0));
            if (expression == null) {
                throw new CompilationException(
                    "Unknown aggregation alias: "
                        + having.text(0)
                        + " in query: "
                        + statement.name());
            }
            return expression;
        }

        private void join(Call join, Call on) {
            requireFollower(join, on, FunctionType.ON);
            query.join(join.text(0), on.text(0) + " " + operator(on.text(1)) + " " + on.text(2));
        }

        private void comparison(
            ConditionList conditions, String connector, String left, Call call) {
            List<String> parameters = new ArrayList<>();
            String condition =
                left + " " + operator(call.text(1)) + " " + bind(call.text(2), parameters);
            conditions.add(connector, condition, parameters);
        }

        private void between(Call call) {
            List<String> parameters = new ArrayList<>();
            String low = bind(call.text(1), parameters);
            String high = bind(call.text(2), parameters);
            query.where().add(AND, call.text(0) + " BETWEEN " + low + " AND " + high, parameters);
        }

        private void in(Call whereIn, Call parameterList) {
            requireFollower(whereIn, parameterList, FunctionType.PARAMETER_LIST);
            List<String> parameters = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            for (String value : parameterList.texts()) {
                placeholders.add(bind(value, parameters));
            }
            query.where()
                .add(
                    AND,
                    whereIn.text(0) + " IN (" + String.join(", ", placeholders) + ")",
                    parameters);
        }

        private void like(Call call, String pattern) {
            query.where().add(AND, call.text(0) + " LIKE ?", List.of(pattern));
        }

        private void subQuery(Call call, String operator) {
            CompiledQuery subQuery = compiledQueries.get(call.text(1));
            if (subQuery == null) {
                throw new CompilationException(
                    "Query: "
                        + call.text(1)
                        + " must be declared before query: "
                        + statement.name());
            }
            query.where()
                .add(
                    AND,
                    call.text(0) + " " + operator + " (" + subQuery.sql() + ")",
                    subQuery.parameters());
        }

        private String bind(String value, List<String> parameters) {
            String date = datesByAlias.get(value);
            if (date != null) {
                parameters.add(date);
                return "TO_DATE(?, 'DD/MM/YYYY')";
            }
            parameters.add(value);
            return "?";
        }

        private String operator(String operator) {
            return operator.toUpperCase(Locale.ROOT);
        }

        private String escapeLike(String value) {
            return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        }

        private void requireFollower(Call leader, Call follower, FunctionType expected) {
            if (follower == null || follower.type() != expected) {
                throw new CompilationException(
                    leader.type().keyword()
                        + " must be followed by "
                        + expected.keyword()
                        + " in query: "
                        + statement.name());
            }
        }
    }
}
