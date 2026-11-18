package com.aabg.notas.infrastructure.persistence.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import javax.sql.DataSource;

/**
 * Configuración de la conexión SQLite para asegurar que las restricciones de
 * integridad referencial (claves foráneas) estén siempre activas.
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
