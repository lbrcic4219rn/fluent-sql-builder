package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresRepository implements Repository {

    private static final int MAX_POOL_SIZE = 5;
    private static final long CONNECTION_TIMEOUT_MS = 10_000L;

    private final HikariDataSource dataSource;

    public PostgresRepository(DatabaseConfig config) {
        this.dataSource = new HikariDataSource(toHikariConfig(config));
    }

    private static HikariConfig toHikariConfig(DatabaseConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.jdbcUrl());
        hikari.setUsername(config.username());
        hikari.setPassword(config.password());
        hikari.setMaximumPoolSize(MAX_POOL_SIZE);
        hikari.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        hikari.setPoolName("bp-postgres");
        hikari.setInitializationFailTimeout(-1);
        return hikari;
    }

    @Override
    public QueryResult query(String sql, List<String> parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setString(i + 1, parameters.get(i));
            }

            try (ResultSet rs = preparedStatement.executeQuery()) {
                return readResult(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private QueryResult readResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        List<String> columns = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            List<Object> row = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
        return new QueryResult(columns, rows);
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
