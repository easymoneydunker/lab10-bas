package lab10.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnection {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnection(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    public void releaseConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
