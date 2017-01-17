/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;

@Startup
@Singleton
public class OcnRepoDatabaseMigrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OcnRepoDatabaseMigrator.class);

    @Resource(lookup = "jdbc/ocn-repo")
    DataSource dataSource;

    public OcnRepoDatabaseMigrator() {}

    public OcnRepoDatabaseMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrate() {
        final Flyway flyway = new Flyway();
        flyway.setTable("schema_version");
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(dataSource);
        for (MigrationInfo info : flyway.info().all()) {
            LOGGER.info("database migration {} : {} from file '{}'",
                    info.getVersion(), info.getDescription(), info.getScript());
        }
        flyway.migrate();
    }
}
