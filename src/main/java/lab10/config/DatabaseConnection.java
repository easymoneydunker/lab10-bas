package lab10.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Организация соединения с БД. Параметры подключения берутся из application.properties,
 * пул соединений (HikariCP) создается Spring Boot.
 */
@Component
public class DatabaseConnection {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnection(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Возвращает соединение с БД (привязанное к текущей транзакции, если она есть).
     * Закрывать через {@link #releaseConnection(Connection)}.
     */
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
