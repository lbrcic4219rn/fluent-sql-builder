package querybuilder.validator.rule;

import querybuilder.parser.Argument;
import querybuilder.parser.Call;
import querybuilder.parser.FunctionType;
import querybuilder.parser.Statement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DateFormatRule implements IRule {

    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @Override
    public List<String> check(Statement statement, Set<String> declaredQueries) {
        List<String> errors = new ArrayList<>();
        for (Call call : statement.callsOf(FunctionType.DATE)) {
            Argument date = call.arguments().getFirst();
            if (!date.quoted()) {
                errors.add(
                    "Date argument (1st) needs to be surrounded by quotes in query: "
                        + statement.name());
            } else if (!isValid(date.text())) {
                errors.add(
                    "Date argument (1st) needs to be a real date in the following format:"
                        + " dd/mm/yyyy for query: "
                        + statement.name());
            }
        }
        return errors;
    }

    private boolean isValid(String date) {
        try {
            LocalDate.parse(date, FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
