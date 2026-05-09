package org.xiaoxu.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.config.MysqlProperties;
import org.xiaoxu.config.RedisProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CheckController {
    private final MysqlProperties mysqlProperties;
    private final RedisProperties redisProperties;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public CheckController(MysqlProperties mysqlProperties,
                           RedisProperties redisProperties,
                           JdbcTemplate jdbcTemplate,
                           DataSource dataSource) {
        this.mysqlProperties = mysqlProperties;
        this.redisProperties = redisProperties;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @GetMapping("/check/config")
    public Map<String, Object> checkConfig() {
        Map<String, Object> result = new HashMap<>();

        result.put("mysqlUrl", mysqlProperties.getUrl());
        result.put("mysqlUsername", mysqlProperties.getUsername());
        result.put("mysqlDriverClassName", mysqlProperties.getDriverClassName());

        result.put("redisHost", redisProperties.getHost());
        result.put("redisPort", redisProperties.getPort());

        // 不要返回真实密码
        result.put("mysqlPasswordLoaded", mysqlProperties.getPassword() != null && !mysqlProperties.getPassword().isBlank());
        result.put("redisPasswordLoaded", redisProperties.getPassword() != null && !redisProperties.getPassword().isBlank());

        return result;
    }

    @GetMapping("/check/mysql")
    public Map<String, Object> checkMysql() throws Exception {
        Map<String, Object> result = new HashMap<>();

        Integer selectResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            result.put("connected", true);
            result.put("selectResult", selectResult);
            result.put("databaseProductName", metaData.getDatabaseProductName());
            result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            result.put("url", metaData.getURL());
            result.put("username", metaData.getUserName());
        }

        return result;
    }
}