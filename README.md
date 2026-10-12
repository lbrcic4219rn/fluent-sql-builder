# fluent-sql-builder

Desktop app that compiles a small fluent query language into parameterised PostgreSQL and shows the
results in a Swing table.

You type something like this:

```
var a = new Query("employees").Select("last_name").Where("job_id", "=", "IT_PROG")
```

and the app validates it, compiles it to `SELECT last_name FROM employees WHERE job_id = ?` with
`IT_PROG` bound as a statement parameter, executes it, and renders the rows.

## Requirements

- JDK 21 (the Gradle toolchain will resolve one)
- Docker, if you want the local PostgreSQL instance

## Configuration

Connection settings come from four environment variables:

| Variable               | Meaning                      |
| ---------------------- | ---------------------------- |
| `BP_POSTGRES_HOST`     | Host, optionally `host:port` |
| `BP_POSTGRES_DATABASE` | Database name                |
| `BP_POSTGRES_USERNAME` | Login                        |
| `BP_POSTGRES_PASSWORD` | Password                     |

`BP_POSTGRES_PORT` is read only by Docker Compose: it is the host port the container is published
on (default 5432). If something else already uses 5432, set it to another port and use the same port
in `BP_POSTGRES_HOST`.

Copy the template and fill in a password:

```bash
cp .env.example .env
```

`.env` is gitignored and never committed. Both Docker Compose and `./gradlew run` read it; variables
already exported in your shell take precedence over the file. If you launch the app any other way,
export the variables yourself.

The app refuses to start, with a message naming what is missing, when host, database or username is
not set.

## Running the database

```bash
docker compose up -d
```

This starts PostgreSQL 17 on `BP_POSTGRES_PORT`, creating the database and user named by
`BP_POSTGRES_DATABASE` and `BP_POSTGRES_USERNAME`. Data persists in the `postgres_data` volume.

The database starts empty. The repository ships no schema or seed data, so create or import your own
tables before running queries. The examples below assume `employees` and `jobs` tables.

## Running the app

```bash
./gradlew run
```

The window has a text area for your query and a confirm button. Validation errors and SQL errors
appear in a dialog; successful queries populate the table, which can be sorted by clicking a column
header. Queries run on a background thread, and the button is disabled until the running query
finishes.

## The query language

A script is one or more statements, each introduced by `var`:

```
var <name> = new Query("<table>").<Function>(...).<Function>(...)
```

The app executes the **last** statement; earlier ones exist so they can be referenced as subqueries.
Text arguments go in double quotes (`\"` and `\\` are the escapes); numbers may be written bare. Every
clause is chained with `.`, and the compiler emits clauses in SQL order regardless of the order you
wrote them.

### Functions

| Function                                    | Arguments            | Emits                           |
| ------------------------------------------- | -------------------- | ------------------------------- |
| `new Query(table)`                          | 1                    | `FROM table`                    |
| `Select(col, ...)`                          | 1 or more            | `SELECT col, ...`               |
| `Where(col, op, value)`                     | 3                    | `col op ?`                      |
| `AndWhere` / `OrWhere`                      | 3                    | `AND` / `OR` condition          |
| `WhereBetween(col, low, high)`              | 3                    | `col BETWEEN ? AND ?`           |
| `WhereIn(col)` + `ParameterList(v, ...)`    | 1 + 1 or more        | `col IN (?, ...)`               |
| `WhereStartsWith` / `EndsWith` / `Contains` | 2                    | `col LIKE ?`                    |
| `WhereInQ(col, queryVar)`                   | 2                    | `col IN (<subquery>)`           |
| `WhereEqQ(col, queryVar)`                   | 2                    | `col = (<subquery>)`            |
| `Join(table)` + `On(a, op, b)`              | 1 + 3                | `JOIN table ON a op b`          |
| `GroupBy(col, ...)`                         | 1 or more            | `GROUP BY col, ...`             |
| `Avg` / `Count` / `Min` / `Max`             | 1 or 2 (col, alias)  | `AVG(col) AS alias`             |
| `Having` / `AndHaving` / `OrHaving`         | 3 (alias, op, value) | `HAVING <aggregate> op ?`       |
| `OrderBy` / `OrderByDesc`                   | 1 or more            | `ORDER BY col, ...`             |
| `Date(dd/mm/yyyy, alias)`                   | 2                    | nothing directly — see below    |

Things worth knowing:

- The first `Where*` function opens the `WHERE` clause and every later one is joined with `AND`, so
  `Where(...).WhereContains(...)` is valid. `OrWhere` is the only way to get an `OR`.
- `Join` names the table being joined, and must be followed directly by `On`. `WhereIn` must be
  followed directly by `ParameterList`.
- `OrderBy` and `OrderByDesc` can be mixed; terms are emitted in the order written.
- Operators are limited to `=`, `<`, `>`, `<=`, `>=`, `<>`, `!=` and `LIKE`.
- `Date` contributes no clause of its own. It declares an alias, and any `Where`, `WhereBetween` or
  `Having` value equal to that alias is compiled to `TO_DATE(?, 'DD/MM/YYYY')`.
- `Having` refers to an aggregate by its alias and is compiled to the aggregate expression itself.
- The values given to `WhereStartsWith`, `WhereEndsWith` and `WhereContains` are matched literally;
  `%` and `_` in them are escaped.

### Examples

Aggregation with a group and a filter on the alias:

```
var a = new Query("employees").Select("job_id").Count("*", "br").GroupBy("job_id").Having("br", ">", 5)
```

A join:

```
var a = new Query("jobs").Select("job_title").Join("employees").On("jobs.job_id", "=", "employees.job_id")
```

A date filter:

```
var a = new Query("employees").Date("01/01/2004", "d").Where("hire_date", ">", "d")
```

A subquery, referencing the earlier variable by name:

```
var a = new Query("jobs").Select("job_id")
var b = new Query("employees").Select("job_id").WhereInQ("job_id", "a")
```

### Validation

Every failure is reported at once rather than stopping at the first. Syntax problems come from the
parser. The rules then check argument counts, that table, column and alias arguments are plain
identifiers and operators are supported, date formatting, the `WhereIn`/`ParameterList` and
`Join`/`On` pairings, that `AndWhere`/`OrWhere` and `AndHaving`/`OrHaving` have something to extend,
aggregate aliases when `HAVING` is used, that `SELECT` columns appear in `GROUP BY` when an aggregate
is present, and that a subquery variable is declared earlier in the script.

## How it works

```
source text → Parser → Validator rules → Compiler → Repository → TableModel
```

`Parser` tokenizes the source and produces a `Statement` per variable, each a list of `Call`s with
their arguments. `FunctionType` is the single registry of functions: keyword, argument count and the
kind of each argument. `QueryBuilder` composes validation and compilation and returns a
`QueryBuildResult` holding either a `CompiledQuery` (SQL plus its ordered parameters), the validation
errors, or a compile error. `AppCore` runs the query and returns a `QueryOutcome`, which the Swing
layer renders.

| Package                  | Responsibility                                               |
| ------------------------ | ------------------------------------------------------------ |
| `app`                    | Entry point and orchestration                                |
| `querybuilder.parser`    | Tokenizer, parser and the function registry                  |
| `querybuilder.validator` | Validation rules that produce a `ValidationResult`           |
| `querybuilder.compiler`  | Turns statements into a parameterised `SELECT`               |
| `database`               | Configuration and a repository over a Hikari connection pool |
| `gui.swingimp`           | Swing frontend                                               |

Neither `querybuilder` nor `database` depends on `app` or `gui`, so the compiler can be exercised
without starting a UI or a database.

Values are always bound as statement parameters. Table, column and alias names are interpolated into
the SQL text, which is why the validator only accepts plain (optionally dotted) identifiers for them.

## Development

| Command                 | Purpose                                         |
| ----------------------- | ----------------------------------------------- |
| `./gradlew build`       | Compile, check formatting and test              |
| `./gradlew run`         | Launch the app                                  |
| `./gradlew format`      | Reformat all sources (google-java-format, AOSP) |
| `./gradlew formatCheck` | Fail if anything is unformatted                 |

### Tests

The two test classes are characterization tests: they run a fixed corpus of queries through the
validator and the compiler and compare the full output against a golden file in
`src/test/resources`. A diff means output changed, not necessarily that it got worse.

To create a new baseline after an intentional change, delete the golden file and run the tests
twice: the first run writes the new baseline and fails, the second verifies it.

