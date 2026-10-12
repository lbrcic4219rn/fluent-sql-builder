package querybuilder.validator;

import querybuilder.parser.ParseResult;
import querybuilder.parser.Parser;
import querybuilder.parser.Statement;
import querybuilder.validator.rule.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Validator implements IValidator {

    private static final List<IRule> SHAPE_RULES =
        List.of(new ArgNumberRule(), new ArgumentFormatRule());

    private static final List<IRule> SEMANTIC_RULES =
        List.of(
            new DateFormatRule(),
            new WhereInParameterListRule(),
            new JoinOnRule(),
            new ConditionChainRule(),
            new AggregationAliasMustExistIfHaving(),
            new OnlyAggregationElementsInHavingRule(),
            new NonAggregationElementsInGroupByRule(),
            new SubQueryVarMustExist());

    private final Parser parser = new Parser();

    @Override
    public ValidationResult validate(String source) {
        ParseResult parsed = parser.parse(source);
        List<String> errors = new ArrayList<>(parsed.errors());
        Set<String> declaredQueries = new HashSet<>();

        for (Statement statement : parsed.statements()) {
            List<String> shapeErrors = run(SHAPE_RULES, statement, declaredQueries);
            errors.addAll(shapeErrors);
            if (shapeErrors.isEmpty()) {
                errors.addAll(run(SEMANTIC_RULES, statement, declaredQueries));
            }
            declaredQueries.add(statement.name());
        }
        return new ValidationResult(parsed.statements(), errors);
    }

    private List<String> run(List<IRule> rules, Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        for (IRule rule : rules) {
            errors.addAll(rule.check(statement, declaredQueries));
        }
        return errors;
    }
}
