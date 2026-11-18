package com.aabg.notas.shared.config;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import javax.sql.DataSource;

/**
 * Configuración de SQLite para habilitar claves foráneas y modo WAL.
 */
@Configuration
public class SqliteConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

        SQLiteDataSource dataSource = new SQLiteDataSource(config);
        dataSource.setUrl(properties.getUrl());
        return dataSource;
    }
}
