package querybuilder.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static querybuilder.parser.ArgumentKind.*;

public enum FunctionType {
    QUERY("Query", 1, false, TABLE),
    SELECT("Select", 1, true, SELECTABLE),
    AVG("Avg", 1, false, SELECTABLE, ALIAS),
    COUNT("Count", 1, false, SELECTABLE, ALIAS),
    MIN("Min", 1, false, SELECTABLE, ALIAS),
    MAX("Max", 1, false, SELECTABLE, ALIAS),
    JOIN("Join", 1, false, TABLE),
    ON("On", 3, false, COLUMN, OPERATOR, COLUMN),
    DATE("Date", 2, false, DATE_LITERAL, ALIAS),
    WHERE("Where", 3, false, COLUMN, OPERATOR, VALUE),
    AND_WHERE("AndWhere", 3, false, COLUMN, OPERATOR, VALUE),
    OR_WHERE("OrWhere", 3, false, COLUMN, OPERATOR, VALUE),
    WHERE_BETWEEN("WhereBetween", 3, false, COLUMN, VALUE, VALUE),
    WHERE_IN("WhereIn", 1, false, COLUMN),
    PARAMETER_LIST("ParameterList", 1, true, VALUE),
    WHERE_STARTS_WITH("WhereStartsWith", 2, false, COLUMN, VALUE),
    WHERE_ENDS_WITH("WhereEndsWith", 2, false, COLUMN, VALUE),
    WHERE_CONTAINS("WhereContains", 2, false, COLUMN, VALUE),
    WHERE_IN_Q("WhereInQ", 2, false, COLUMN, QUERY_NAME),
    WHERE_EQ_Q("WhereEqQ", 2, false, COLUMN, QUERY_NAME),
    GROUP_BY("GroupBy", 1, true, COLUMN),
    HAVING("Having", 3, false, ALIAS, OPERATOR, VALUE),
    AND_HAVING("AndHaving", 3, false, ALIAS, OPERATOR, VALUE),
    OR_HAVING("OrHaving", 3, false, ALIAS, OPERATOR, VALUE),
    ORDER_BY("OrderBy", 1, true, COLUMN),
    ORDER_BY_DESC("OrderByDesc", 1, true, COLUMN);

    private static final Map<String, FunctionType> BY_KEYWORD =
        Arrays.stream(values())
            .collect(Collectors.toMap(FunctionType::keyword, Function.identity()));

    private final String keyword;
    private final int minArguments;
    private final boolean variadic;
    private final List<ArgumentKind> kinds;

    FunctionType(String keyword, int minArguments, boolean variadic, ArgumentKind... kinds) {
        this.keyword = keyword;
        this.minArguments = minArguments;
        this.variadic = variadic;
        this.kinds = List.of(kinds);
    }

    public static Optional<FunctionType> fromKeyword(String keyword) {
        return Optional.ofNullable(BY_KEYWORD.get(keyword));
    }

    public String keyword() {
        return keyword;
    }

    public int minArguments() {
        return minArguments;
    }

    public int maxArguments() {
        return variadic ? Integer.MAX_VALUE : kinds.size();
    }

    public boolean accepts(int argumentCount) {
        return argumentCount >= minArguments && argumentCount <= maxArguments();
    }

    public ArgumentKind kindAt(int index) {
        return kinds.get(Math.min(index, kinds.size() - 1));
    }

    public boolean isAggregation() {
        return this == AVG || this == COUNT || this == MIN || this == MAX;
    }

    public boolean startsWhere() {
        return switch (this) {
            case WHERE,
                 WHERE_BETWEEN,
                 WHERE_IN,
                 WHERE_STARTS_WITH,
                 WHERE_ENDS_WITH,
                 WHERE_CONTAINS,
                 WHERE_IN_Q,
                 WHERE_EQ_Q -> true;
            default -> false;
        };
    }

    public boolean continuesWhere() {
        return this == AND_WHERE || this == OR_WHERE;
    }

    public boolean continuesHaving() {
        return this == AND_HAVING || this == OR_HAVING;
    }

    public boolean isHaving() {
        return this == HAVING || continuesHaving();
    }

    public boolean isSubQuery() {
        return this == WHERE_IN_Q || this == WHERE_EQ_Q;
    }
}
