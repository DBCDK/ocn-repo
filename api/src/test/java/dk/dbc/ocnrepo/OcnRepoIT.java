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
import javax.persistence.NoResultException;
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
import java.util.List;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        final OcnRepo ocnRepo = ocnRepo();
        final List<WorldCatEntity> result = ocnRepo.lookupWorldCatEntity(new WorldCatEntity()
                                                            .withPid("870970-basis:44260441"));
        assertThat("Number of results", result.size(), is(1));
    }

    @Test
    public void worldcatEntityLookedUpByAgencyIdAndBibliographicRecordId() {
        final OcnRepo ocnRepo = ocnRepo();
        final List<WorldCatEntity> result = ocnRepo.lookupWorldCatEntity(new WorldCatEntity()
                                                            .withAgencyId(870970)
                                                            .withBibliographicRecordId("44260441"));
        assertThat("Number of results", result.size(), is(1));
        assertThat("default checksum", result.get(0).getChecksum(), is(0));
    }

    @Test
    public void getPidListFromOcn() {
        final OcnRepo ocnRepo = ocnRepo();
        final String ocn = "871992862";
        final List<String> result = ocnRepo.pidListFromOcn(ocn);

        assertThat("number of results", result.size(), is(2));
        assertThat("pid", result.get(0), is("870970-basis:44260441"));
        assertThat("pid", result.get(1), is("870970-basis:44260442"));
    }

    @Test
    public void getOcnByPid() {
        final OcnRepo ocnRepo = ocnRepo();
        final String pid = "870970-basis:44260441";
        final String ocn = ocnRepo.getOcnByPid(pid);

        assertThat("ocn", ocn, is("871992862"));
    }

    @Test(expected = NoResultException.class)
    public void getOcnByPid_noResultsFound() {
        final OcnRepo ocnRepo = ocnRepo();
        final String pid = "noSuchPid";
        ocnRepo.getOcnByPid(pid);
        fail("no exception thrown");
    }

    private OcnRepo ocnRepo() {
        return new OcnRepo(entityManager);
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
