package lab10.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnection(DataSource dataSource,
                              @Value("${spring.datasource.url}") String url,
                              @Value("${spring.datasource.username}") String user,
                              @Value("${spring.datasource.password}") String password) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getJdbcConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
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
