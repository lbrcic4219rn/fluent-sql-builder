package database;

import java.util.List;

public interface Repository extends AutoCloseable {

    QueryResult query(String sql, List<String> parameters);

    @Override
    void close();
}
