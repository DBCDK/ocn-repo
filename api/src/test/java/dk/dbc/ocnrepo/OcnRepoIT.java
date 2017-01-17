/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo;

import dk.dbc.commons.jdbc.util.JDBCUtil;
import dk.dbc.ocnrepo.dto.WorldCatEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OcnRepoIT {
    private static final PGSimpleDataSource datasource;

    static {
        datasource = new PGSimpleDataSource();
        datasource.setDatabaseName("ocnrepo");
        datasource.setServerName("localhost");
        datasource.setPortNumber(getPostgresqlPort());
        datasource.setUser(System.getProperty("user.name"));
        datasource.setPassword(System.getProperty("user.name"));
    }

    private static Map<String, String> entityManagerProperties = new HashMap<>();
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeClass
    public static void migrateDatabase() throws Exception {
        final OcnRepoDatabaseMigrator dbMigrator = new OcnRepoDatabaseMigrator(datasource);
        dbMigrator.migrate();
    }

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerProperties.put(JDBC_USER, datasource.getUser());
        entityManagerProperties.put(JDBC_PASSWORD, datasource.getPassword());
        entityManagerProperties.put(JDBC_URL, datasource.getUrl());
        entityManagerProperties.put(JDBC_DRIVER, "org.postgresql.Driver");
        entityManagerProperties.put("eclipselink.logging.level", "FINE");
        entityManagerFactory = Persistence.createEntityManagerFactory("ocnRepoIT", entityManagerProperties);
    }

    @Before
    public void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager(entityManagerProperties);
    }

    @Before
    public void resetDatabase() throws SQLException {
        try (Connection conn = datasource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM worldcat");
        }
    }

    @Before
    public void populateDatabase() throws URISyntaxException {
        executeScriptResource("/populate.sql");
    }

    @After
    public void clearEntityManagerCache() {
        entityManager.clear();
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Test
    public void worldcatEntityLookedUpByPid() {
        assertThat(entityManager.createNamedQuery(WorldCatEntity.GET_BY_PID_QUERY_NAME, WorldCatEntity.class)
                        .setParameter("pid", "870970-basis:44260441")
                        .getResultList()
                        .stream()
                        .findFirst()
                .       orElse(null),
            is(notNullValue()));
    }

    @Test
    public void worldcatEntityLookedUpByAgencyIdAndBibliographicRecordId() {
        assertThat(entityManager.createNamedQuery(WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME, WorldCatEntity.class)
                        .setParameter("agencyId", 870970)
                        .setParameter("bibliographicRecordId", "44260441")
                        .getResultList()
                        .stream()
                        .findFirst()
                        .orElse(null),
            is(notNullValue()));
    }

    private static int getPostgresqlPort() {
        final String port = System.getProperty("postgresql.port");
        if (port != null && !port.isEmpty()) {
            return Integer.parseInt(port);
        }
        return 5432;
    }

    private static void executeScriptResource(String resourcePath) {
        final URL resource = OcnRepoIT.class.getResource(resourcePath);
        try {
            executeScript(new File(resource.toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void executeScript(File scriptFile) {
        try (Connection conn = datasource.getConnection()) {
            JDBCUtil.executeScript(conn, scriptFile, StandardCharsets.UTF_8.name());
        } catch (SQLException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}