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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class OcnRepoIT extends JpaIntegrationTest {
    @Override
    public JpaTestEnvironment setup() {
        final PGSimpleDataSource dataSource = getDataSource();
        migrateDatabase(dataSource);
        jpaTestEnvironment = new JpaTestEnvironment(dataSource, "ocnRepoIT");
        return jpaTestEnvironment;
    }

    @Before
    public void resetDatabase() throws SQLException {
        try (Connection conn = jpaTestEnvironment.getDatasource().getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM worldcat");
        }
    }

    @Before
    public void populateDatabase() throws URISyntaxException {
        executeScriptResource("/populate.sql");
    }

    @Test
    public void worldcatEntityLookedUpByPid() {
        final OcnRepo ocnRepo = ocnRepo();
        final List<WorldCatEntity> result = ocnRepo.lookupWorldCatEntity(new WorldCatEntity()
                                                            .withPid("870970-basis:44260441"));
        assertThat("number of results", result.size(), is(1));
    }

    @Test
    public void worldcatEntityLookedUpByAgencyIdAndBibliographicRecordId() {
        final OcnRepo ocnRepo = ocnRepo();
        final List<WorldCatEntity> result = ocnRepo.lookupWorldCatEntity(new WorldCatEntity()
                                                            .withAgencyId(870970)
                                                            .withBibliographicRecordId("44260441"));
        assertThat("number of results", result.size(), is(1));
        assertThat("default checksum", result.get(0).getChecksum(), is(nullValue()));
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
        final Optional<String> ocn = ocnRepo.getOcnByPid(pid);

        assertThat("is present", ocn.isPresent(), is(true));
        assertThat("ocn", ocn.get(), is("871992862"));
    }

    @Test
    public void getOcnByPid_noResultsFound() {
        final OcnRepo ocnRepo = ocnRepo();
        final String pid = "noSuchPid";
        final Optional<String> ocn = ocnRepo.getOcnByPid(pid);

        assertThat("is not present", ocn.isPresent(), is(false));
    }

    @Test
    public void getEntitiesWithLHR() {
        final OcnRepo ocnRepo = ocnRepo();
        final List<WorldCatEntity> result = new ArrayList<>();
        ocnRepo.getEntitiesWithLHR().forEach(result::add);

        assertThat("number of results", result.size(), is(2));
        assertThat("pid", result.get(0).getPid(), is("870970-basis:44260443"));
    }

    @Test
    public void activeHoldingSymbols() {
        final WorldCatEntity entityBeforeUpdate = jpaTestEnvironment.getEntityManager()
                .find(WorldCatEntity.class, "870970-basis:44260441");
        assertThat("active holding symbols read", entityBeforeUpdate.getActiveHoldingSymbols(),
                is(Arrays.asList("ABC", "DEF")));

        jpaTestEnvironment.getPersistenceContext().run(() ->
                entityBeforeUpdate.withActiveHoldingSymbols(Collections.singletonList("GHI")));

        jpaTestEnvironment.clearEntityManagerCache();

        final WorldCatEntity entityAfterUpdate = jpaTestEnvironment.getEntityManager()
                .find(WorldCatEntity.class, "870970-basis:44260441");
        assertThat("active holding symbols written", entityAfterUpdate.getActiveHoldingSymbols(),
                is(Collections.singletonList("GHI")));
    }

    @Test
    public void timestamps() {
        final WorldCatEntity entity = new WorldCatEntity()
                .withPid("123456-test:id")
                .withAgencyId(123456)
                .withBibliographicRecordId("id");

        env().getPersistenceContext().run(() -> env().getEntityManager().persist(entity));

        final Instant createdInitially = entity.getCreated();
        final Instant modifiedInitially = entity.getModified();
        assertThat("created after persist", createdInitially, is(notNullValue()));
        assertThat("modified after persist", modifiedInitially, is(createdInitially));

        env().getPersistenceContext().run(() -> entity.withOcn("ocn"));

        assertThat("created after update", entity.getCreated(), is(createdInitially));
        assertThat("modified after update", entity.getModified(), is(not(modifiedInitially)));
    }

    private OcnRepo ocnRepo() {
        return new OcnRepo(jpaTestEnvironment.getEntityManager());
    }

    private static int getPostgresqlPort() {
        final String port = System.getProperty("postgresql.port");
        if (port != null && !port.isEmpty()) {
            return Integer.parseInt(port);
        }
        return 5432;
    }

    private PGSimpleDataSource getDataSource() {
        final PGSimpleDataSource datasource = new PGSimpleDataSource();
        datasource.setDatabaseName("ocnrepo");
        datasource.setServerName("localhost");
        datasource.setPortNumber(getPostgresqlPort());
        datasource.setUser(System.getProperty("user.name"));
        datasource.setPassword(System.getProperty("user.name"));
        return datasource;
    }

    private void migrateDatabase(PGSimpleDataSource datasource) {
        final OcnRepoDatabaseMigrator dbMigrator = new OcnRepoDatabaseMigrator(datasource);
        dbMigrator.migrate();
    }
}
