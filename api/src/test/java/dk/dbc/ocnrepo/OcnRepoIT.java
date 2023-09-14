/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo;

import dk.dbc.commons.persistence.JpaIntegrationTest;
import dk.dbc.commons.persistence.JpaTestEnvironment;
import dk.dbc.ocnrepo.dto.WorldCatEntity;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class OcnRepoIT extends JpaIntegrationTest {
    private static int getPostgresqlPort() {
        String port = System.getProperty("postgresql.port");
        if (port != null && !port.isEmpty()) {
            return Integer.parseInt(port);
        }
        return 5432;
    }

    @Override
    public JpaTestEnvironment setup() {
        PGSimpleDataSource dataSource = getDataSource();
        migrateDatabase(dataSource);
        jpaTestEnvironment = new JpaTestEnvironment(dataSource, "ocnRepoIT");
        return jpaTestEnvironment;
    }

    @Before
    public void resetDatabase() throws SQLException {
        try (Connection conn = jpaTestEnvironment.getDatasource().getConnection(); Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM worldcat");
        }
    }

    @Before
    public void populateDatabase() throws URISyntaxException {
        executeScriptResource("/populate.sql");
    }

    @Test
    public void worldcatEntityLookedUpByPid() {
        OcnRepo ocnRepo = ocnRepo();
        List<WorldCatEntity> result = ocnRepo.lookupWorldCatEntity(new WorldCatEntity().withPid("870970-basis:44260441"));
        assertEquals("number of results", 1, result.size());
    }

    @Test
    public void worldcatEntityLookedUpByAgencyIdAndBibliographicRecordId() {
        OcnRepo ocnRepo = ocnRepo();
        List<WorldCatEntity> result = ocnRepo.lookupWorldCatEntity(new WorldCatEntity().withAgencyId(870970).withBibliographicRecordId("44260441"));
        assertEquals("number of results", 1, result.size());
        assertNull("default checksum", result.get(0).getChecksum());
    }

    @Test
    public void getPidListFromOcn() {
        OcnRepo ocnRepo = ocnRepo();
        final String ocn = "871992862";
        List<String> result = ocnRepo.pidListFromOcn(ocn);
        assertEquals("pid", List.of("870970-basis:44260441", "870970-basis:44260442"), result);
    }

    @Test
    public void getOcnByPid() {
        OcnRepo ocnRepo = ocnRepo();
        final String pid = "870970-basis:44260441";
        String ocn = ocnRepo.getOcnByPid(pid).orElse(null);
        assertEquals("ocn", "871992862", ocn);
    }

    @Test
    public void getOcnByPid_noResultsFound() {
        OcnRepo ocnRepo = ocnRepo();
        final String pid = "noSuchPid";
        Optional<String> ocn = ocnRepo.getOcnByPid(pid);
        assertFalse("is not present", ocn.isPresent());
    }

    @Test
    public void getEntitiesWithLHR() {
        OcnRepo ocnRepo = ocnRepo();
        List<WorldCatEntity> result = new ArrayList<>();
        ocnRepo.getEntitiesWithLHR().forEach(result::add);

        assertEquals("number of results", 2, result.size());
        assertEquals("pid", "870970-basis:44260443", result.get(0).getPid());
    }

    @Test
    public void activeHoldingSymbols() {
        WorldCatEntity entityBeforeUpdate = jpaTestEnvironment.getEntityManager().find(WorldCatEntity.class, "870970-basis:44260441");
        assertEquals("active holding symbols read", List.of("ABC", "DEF"), entityBeforeUpdate.getActiveHoldingSymbols());

        jpaTestEnvironment.getPersistenceContext().run(() -> entityBeforeUpdate.withActiveHoldingSymbols(Collections.singletonList("GHI")));

        jpaTestEnvironment.clearEntityManagerCache();

        WorldCatEntity entityAfterUpdate = jpaTestEnvironment.getEntityManager().find(WorldCatEntity.class, "870970-basis:44260441");
        assertEquals("active holding symbols written", List.of("GHI"), entityAfterUpdate.getActiveHoldingSymbols());
    }

    @Test
    public void timestamps() {
        WorldCatEntity entity = new WorldCatEntity().withPid("123456-test:id").withAgencyId(123456).withBibliographicRecordId("id");

        env().getPersistenceContext().run(() -> env().getEntityManager().persist(entity));

        Instant createdInitially = entity.getCreated();
        Instant modifiedInitially = entity.getModified();
        assertNotNull("created after persist", createdInitially);
        assertEquals("modified after persist", createdInitially, modifiedInitially);

        env().getPersistenceContext().run(() -> entity.withOcn("ocn"));

        assertEquals("created after update", entity.getCreated(), createdInitially);
        assertNotEquals("modified after update", modifiedInitially, entity.getModified());
    }

    private OcnRepo ocnRepo() {
        return new OcnRepo(jpaTestEnvironment.getEntityManager());
    }

    private PGSimpleDataSource getDataSource() {
        PGSimpleDataSource datasource = new PGSimpleDataSource();
        datasource.setDatabaseName("ocnrepo");
        datasource.setServerName("localhost");
        datasource.setPortNumber(getPostgresqlPort());
        datasource.setUser(System.getProperty("user.name"));
        datasource.setPassword(System.getProperty("user.name"));
        return datasource;
    }

    private void migrateDatabase(PGSimpleDataSource datasource) {
        OcnRepoDatabaseMigrator dbMigrator = new OcnRepoDatabaseMigrator(datasource);
        dbMigrator.migrate();
    }
}
