package querybuilder.validator;

import org.junit.jupiter.api.Test;
import querybuilder.parser.Call;
import querybuilder.parser.Statement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterizationTest {

    private static final Path GOLDEN = Path.of("src", "test", "resources", "golden.txt");

    private static final List<String> CORPUS =
        List.of(
            "var a = new Query(\"departments\").Select(\"manager_id\", \"location_id\")",
            "var a = new"
                + " Query(\"departments\").Select(\"manager_id\").OrderBy(\"manager_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").OrderByDesc(\"last_name\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").Where(\"job_id\",\"=\",\"IT_PROG\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").Where(\"job_id\",\"=\",\"IT_PROG\").AndWhere(\"manager_id\",\"=\",\"100\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").Where(\"job_id\",\"=\",\"IT_PROG\").OrWhere(\"manager_id\",\"=\",100)",
            "var a = new"
                + " Query(\"employees\").Select(\"min_salary\").WhereBetween(\"min_salary\",\"1000\",\"5000\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereIn(\"job_id\").ParameterList(\"IT_PROG\",\"SA_REP\")",
            "var a = new Query(\"employees\").Select(\"last_name\").WhereIn(\"job_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").ParameterList(\"IT_PROG\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereIn(\"job_id\").OrderBy(\"last_name\").ParameterList(\"IT_PROG\")",
            "var a = new"
                + " Query(\"jobs\").Select(\"job_title\").Join(\"employees\").On(\"jobs.job_id\",\"=\",\"employees.job_id\")",
            "var a = new Query(\"jobs\").Select(\"job_title\").Join(\"employees\")",
            "var a = new"
                + " Query(\"jobs\").Select(\"job_title\").On(\"jobs.job_id\",\"=\",\"employees.job_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Having(\"br\",\">\",\"5\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\").AndHaving(\"br\",\"<\",\"9\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\").OrHaving(\"br\",\"<\",\"9\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").AndHaving(\"br\",\"<\",\"9\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"job_id\",\">\",\"5\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\")",
            "var a = new"
                + " Query(\"employees\").Select(\"min_salary\").Avg(\"min_salary\",\"prosek\")",
            "var a = new Query(\"employees\").Max(\"min_salary\",\"maks\")",
            "var a = new Query(\"employees\").Min(\"min_salary\")",
            "var a = new"
                + " Query(\"employees\").Select(\"hire_date\").Date(\"17/06/2003\",\"d\").Where(\"hire_date\",\">\",\"d\")",
            "var a = new"
                + " Query(\"employees\").Select(\"hire_date\").Date(\"hire_date\",\"17/06/2003\")",
            "var a = new"
                + " Query(\"employees\").Select(\"hire_date\").Date(\"2003-06-17\",\"d\")",
            "var a = new"
                + " Query(\"employees\").Select(\"hire_date\").Date(\"31/02/2003\",\"d\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereStartsWith(\"last_name\",\"A\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereEndsWith(\"last_name\",\"z\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereContains(\"last_name\",\"an\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").AndWhere(\"job_id\",\"=\",\"IT_PROG\")",
            "var a = new Query(\"jobs\").Select(\"job_id\") var b = new"
                + " Query(\"employees\").Select(\"job_id\").WhereInQ(\"job_id\",\"a\")",
            "var a = new Query(\"jobs\").Select(\"job_id\")\n"
                + "var b = new"
                + " Query(\"employees\").Select(\"job_id\").WhereEqQ(\"job_id\",\"a\")",
            "var b = new"
                + " Query(\"employees\").Select(\"job_id\").WhereInQ(\"job_id\",\"zzz\")",
            "var b = new Query(\"employees\").WhereInQ(\"job_id\",\"a\") var a = new"
                + " Query(\"jobs\").Select(\"job_id\")",
            "var a = new Query(\"jobs\") var a = new Query(\"employees\")",
            "new Query(\"departments\").Select(\"manager_id\")",
            "",
            "x var a = new Query(\"departments\")",
            "var a = new Query(\"departments\"",
            "var a = new Query().Select()",
            "var a = new Query(\"departments\").Select()",
            "var 1a = new Query(\"departments\").Select(\"manager_id\")",
            "var = new Query(\"departments\")",
            "var a new Query(\"departments\")",
            "var a = Query(\"departments\")",
            "var a = new Select(\"manager_id\")",
            "var a = new Query(\"departments\").Bogus(\"x\")",
            "var a = new Query(\"departments\").Select(\"manager_id\")"
                + " OrderBy(\"manager_id\")",
            "var a = new Query(\"departments\").Select(manager_id)",
            "var a = new Query(\"departments\").Select(\"manager_id\" \"location_id\")",
            "var a = new Query(\"departments\").Select(\"manager_id)",
            "var a = new Query(\"departments\").Where(\"manager_id\",\"=\")",
            "var a = new Query(\"departments\").Where(\"manager_id\",\"; DROP\",\"1\")",
            "var a = new Query(\"departments; DROP TABLE x\").Select(\"manager id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"variable\").Where(\"last_name\",\"=\",\"var"
                + " x) = .Where(\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\",\"last_name\").Count(\"employees\",\"br\").GroupBy(\"job_id\")");

    private String report() {
        Validator validator = new Validator();
        StringBuilder sb = new StringBuilder();
        for (String source : CORPUS) {
            ValidationResult result = validator.validate(source);
            sb.append("SOURCE: ").append(source.replace("\n", "\\n")).append('\n');
            for (Statement statement : result.statements()) {
                sb.append("  STATEMENT ").append(statement.name()).append(':');
                for (Call call : statement.calls()) {
                    sb.append(' ').append(call.type().keyword()).append(call.texts());
                }
                sb.append('\n');
            }
            for (String error : result.errors()) {
                sb.append("  ERROR: ").append(error).append('\n');
            }
        }
        return sb.toString();
    }

    @Test
    void behaviourMatchesGolden() throws IOException {
        String actual = report();
        if (!Files.exists(GOLDEN)) {
            Files.createDirectories(GOLDEN.getParent());
            Files.writeString(GOLDEN, actual);
            throw new IOException("golden baseline created at " + GOLDEN + "; re-run to verify");
        }
        assertEquals(Files.readString(GOLDEN), actual);
    }
}
