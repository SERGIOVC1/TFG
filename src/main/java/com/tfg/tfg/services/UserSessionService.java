package com.tfg.tfg.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class UserSessionService {

    private final JdbcTemplate jdbcTemplate;

    public UserSessionService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setUserIdForSession(String userId) {
        String sql = "SET app.current_user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    // Simula una operación para que el trigger se dispare (ejemplo insertar un registro)
    public void registerAction(String userId, String tableName, String action, String details) {
        setUserIdForSession(userId);

        // Aquí harías la inserción real en alguna tabla monitorizada por el trigger
        // Por ejemplo, insertar en una tabla de logs o similar para que el trigger capture

        // Ejemplo sencillo:
        String sql = "INSERT INTO some_monitored_table (table_name, action, details) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, tableName, action, details);
    }
}
