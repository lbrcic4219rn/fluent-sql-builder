package querybuilder.validator.rule;

import querybuilder.parser.Statement;

import java.util.List;
import java.util.Set;

public interface IRule {

    List<String> check(Statement statement, Set<String> declaredQueries);
}
