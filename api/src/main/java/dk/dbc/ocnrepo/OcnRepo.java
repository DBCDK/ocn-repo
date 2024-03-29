/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo;

import dk.dbc.commons.jdbc.util.CursoredResultSet;
import dk.dbc.ocnrepo.dto.WorldCatEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class contains the ocn repository API
 */
@Stateless
public class OcnRepo {
    @PersistenceContext(unitName = "ocnRepoPU")
    EntityManager entityManager;

    public OcnRepo() {
    }

    public OcnRepo(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Tries to lookup {@link WorldCatEntity}s in repository either by PID
     * or by agencyId/bibliographicRecordId combination
     * @param value values placeholder
     * @return list of managed WorldCatEntity objects, or empty list if none could be found
     */
    public List<WorldCatEntity> lookupWorldCatEntity(WorldCatEntity value) {
        List<WorldCatEntity> result = new ArrayList<>();
        if (value != null) {
            if (value.getPid() != null) {
                WorldCatEntity entity = entityManager.find(WorldCatEntity.class, value.getPid());
                if (entity != null) {
                    result.add(entity);
                }
            } else if (value.getAgencyId() != null && value.getBibliographicRecordId() != null) {
                result.addAll(entityManager.createNamedQuery(WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME, WorldCatEntity.class)
                        .setParameter("agencyId", value.getAgencyId()).setParameter("bibliographicRecordId", value.getBibliographicRecordId())
                        .getResultList());
            }
        }
        return result;
    }

    /**
     * Gets a list of pids by an ocn
     * @param ocn the ocn to look up
     * @return a list of pids
     */
    public List<String> pidListFromOcn(String ocn) {
        return entityManager.createNamedQuery(WorldCatEntity.GET_PID_LIST_BY_OCN_QUERY_NAME, String.class).setParameter("ocn", ocn).getResultList();
    }

    /**
     * Gets an ocn by pid
     * @param pid the pid to look up
     * @return an ocn
     */
    public Optional<String> getOcnByPid(String pid) {
        try {
            String ocn = entityManager.createNamedQuery(WorldCatEntity.GET_OCN_BY_PID_QUERY_NAME, String.class)
                    .setParameter("pid", pid).getSingleResult();
            return Optional.of(ocn);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a result set of worldcat entities with local holdings records
     * @return result set of entities with lhr
     */
    public CursoredResultSet<WorldCatEntity> getEntitiesWithLHR() {
        Query query = entityManager.createNamedQuery(WorldCatEntity.GET_ENTITIES_WITH_LHR_QUERY_NAME, WorldCatEntity.class);
        return new CursoredResultSet<>(query);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
