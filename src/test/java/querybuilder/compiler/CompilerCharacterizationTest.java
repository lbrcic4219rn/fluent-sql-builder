package querybuilder.compiler;

import org.junit.jupiter.api.Test;
import querybuilder.parser.ParseResult;
import querybuilder.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompilerCharacterizationTest {

    private static final Path GOLDEN = Path.of("src", "test", "resources", "golden-compiler.txt");

    private static final List<String> CORPUS =
        List.of(
            "var a = new Query(\"departments\")",
            "var a = new Query(\"departments\").Select(\"manager_id\",\"location_id\")",
            "var a = new"
                + " Query(\"departments\").Select(\"manager_id\").OrderBy(\"manager_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").OrderByDesc(\"last_name\")",
            "var a = new"
                + " Query(\"employees\").OrderByDesc(\"last_name\",\"first_name\").OrderBy(\"salary\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").Where(\"job_id\",\"=\",\"IT_PROG\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").Where(\"job_id\",\"=\",\"IT_PROG\").AndWhere(\"manager_id\",\"=\",\"100\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").Where(\"job_id\",\"=\",\"IT_PROG\").OrWhere(\"manager_id\",\"=\",100)",
            "var a = new Query(\"employees\").Where(\"last_name\",\"=\",\"De Haan\")",
            "var a = new Query(\"employees\").Where(\"last_name\",\"like\",\"a, b)"
                + " \\\"c\\\"\")",
            "var a = new"
                + " Query(\"employees\").Where(\"job_id\",\"=\",\"IT_PROG\").WhereContains(\"last_name\",\"an\").WhereBetween(\"salary\",1000,5000.5)",
            "var a = new"
                + " Query(\"employees\").Select(\"min_salary\").WhereBetween(\"min_salary\",\"1000\",\"5000\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereIn(\"job_id\").ParameterList(\"IT_PROG\",\"SA_REP\")",
            "var a = new Query(\"employees\").Select(\"last_name\").WhereIn(\"job_id\")",
            "var a = new"
                + " Query(\"jobs\").Select(\"job_title\").Join(\"employees\").On(\"jobs.job_id\",\"=\",\"employees.job_id\")",
            "var a = new"
                + " Query(\"jobs\").Join(\"employees\").On(\"jobs.job_id\",\"=\",\"employees.job_id\").Join(\"departments\").On(\"employees.department_id\",\"=\",\"departments.department_id\").Where(\"jobs.min_salary\",\">\",\"1000\")",
            "var a = new Query(\"jobs\").Select(\"job_title\").Join(\"employees\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\",\"manager_id\").Count(\"*\",\"br\").GroupBy(\"job_id\",\"manager_id\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\").AndHaving(\"br\",\"<\",\"9\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\").OrHaving(\"br\",\"<\",\"9\")",
            "var a = new"
                + " Query(\"employees\").Where(\"salary\",\">\",\"100\").Select(\"job_id\").Count(\"employees\",\"br\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\").OrderBy(\"br\")",
            "var a = new"
                + " Query(\"employees\").Select(\"job_id\").GroupBy(\"job_id\").Having(\"br\",\">\",\"5\")",
            "var a = new"
                + " Query(\"employees\").Select(\"min_salary\").Avg(\"min_salary\",\"prosek\")",
            "var a = new Query(\"employees\").Max(\"min_salary\",\"maks\")",
            "var a = new Query(\"employees\").Min(\"min_salary\").Max(\"min_salary\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereStartsWith(\"last_name\",\"A\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereEndsWith(\"last_name\",\"z\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereContains(\"last_name\",\"an\")",
            "var a = new"
                + " Query(\"employees\").Select(\"last_name\").WhereContains(\"last_name\",\"50%_\")",
            "var a = new"
                + " Query(\"employees\").Select(\"hire_date\").Date(\"17/06/2003\",\"d\").Where(\"hire_date\",\">\",\"d\")",
            "var a = new"
                + " Query(\"employees\").Date(\"17/06/2003\",\"from\").Date(\"17/06/2005\",\"to\").WhereBetween(\"hire_date\",\"from\",\"to\")",
            "var a = new"
                + " Query(\"jobs\").Select(\"job_id\").Where(\"min_salary\",\">\",\"1000\")"
                + " var b = new"
                + " Query(\"employees\").Select(\"job_id\").Where(\"salary\",\"<\",\"9000\").WhereInQ(\"job_id\",\"a\").AndWhere(\"manager_id\",\"=\",\"100\")",
            "var a = new Query(\"jobs\").Max(\"min_salary\") var b = new"
                + " Query(\"employees\").Select(\"last_name\").WhereEqQ(\"salary\",\"a\")",
            "var b = new"
                + " Query(\"employees\").Select(\"job_id\").WhereInQ(\"job_id\",\"zzz\")");

    private String report() {
        Parser parser = new Parser();
        Compiler compiler = new Compiler();
        StringBuilder sb = new StringBuilder();
        for (String source : CORPUS) {
            sb.append("SOURCE: ").append(source).append('\n');
            ParseResult parsed = parser.parse(source);
            for (String error : parsed.errors()) {
                sb.append("  PARSE ERROR: ").append(error).append('\n');
            }
            CompilationResult result = compiler.compile(parsed.statements());
            if (result.isSuccess()) {
                sb.append("  SQL: ").append(result.query().sql()).append('\n');
                sb.append("  PARAMS: ").append(result.query().parameters()).append('\n');
            } else {
                sb.append("  ERROR: ").append(result.error()).append('\n');
            }
        }
        return sb.toString();
    }

    @Test
    void compilerOutputMatchesGolden() throws IOException {
        String actual = report();
        if (!Files.exists(GOLDEN)) {
            Files.createDirectories(GOLDEN.getParent());
            Files.writeString(GOLDEN, actual);
            throw new IOException("golden baseline created at " + GOLDEN + "; re-run to verify");
        }
        assertEquals(Files.readString(GOLDEN), actual);
    }
}
