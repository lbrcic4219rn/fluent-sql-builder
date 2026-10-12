package database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record DatabaseConfig(String host, String database, String username, String password) {

    private static final String HOST_VARIABLE = "BP_POSTGRES_HOST";
    private static final String DATABASE_VARIABLE = "BP_POSTGRES_DATABASE";
    private static final String USERNAME_VARIABLE = "BP_POSTGRES_USERNAME";
    private static final String PASSWORD_VARIABLE = "BP_POSTGRES_PASSWORD";

    public static DatabaseConfig fromEnvironment() {
        return from(System.getenv());
    }

    public static DatabaseConfig from(Map<String, String> environment) {
        List<String> missing = new ArrayList<>();
        for (String name : List.of(HOST_VARIABLE, DATABASE_VARIABLE, USERNAME_VARIABLE)) {
            String value = environment.get(name);
            if (value == null || value.isBlank()) {
                missing.add(name);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                "Missing environment variables: "
                    + String.join(", ", missing)
                    + ". Copy .env.example to .env and start the app with ./gradlew run,"
                    + " or export them in your shell.");
        }
        return new DatabaseConfig(
            environment.get(HOST_VARIABLE).trim(),
            environment.get(DATABASE_VARIABLE).trim(),
            environment.get(USERNAME_VARIABLE).trim(),
            environment.getOrDefault(PASSWORD_VARIABLE, ""));
    }

    public String jdbcUrl() {
        return "jdbc:postgresql://" + host + "/" + database + "?stringtype=unspecified";
    }

    @Override
    public String toString() {
        return "DatabaseConfig[host="
            + host
            + ", database="
            + database
            + ", username="
            + username
            + "]";
    }
}
