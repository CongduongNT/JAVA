package com.planbookai.backend.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DatabaseLessonPlanFrameworkValidator implements LessonPlanFrameworkValidator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseLessonPlanFrameworkValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void validateFrameworkIdIfAvailable(Integer frameworkId) {
        if (frameworkId == null || !curriculumFrameworkTableExists()) {
            return;
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM curriculum_frameworks WHERE id = ?",
                Integer.class,
                frameworkId);

        if (count == null || count == 0) {
            throw new IllegalArgumentException("frameworkId not found: " + frameworkId);
        }
    }

    private boolean curriculumFrameworkTableExists() {
        try {
            Boolean exists = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
                DatabaseMetaData metaData = connection.getMetaData();
                return hasTable(metaData, connection.getCatalog(), "curriculum_frameworks")
                        || hasTable(metaData, connection.getCatalog(), "CURRICULUM_FRAMEWORKS");
            });
            return Boolean.TRUE.equals(exists);
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private boolean hasTable(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }
}
