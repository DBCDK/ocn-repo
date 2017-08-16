/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo;

import dk.dbc.ocnrepo.dto.WorldCatEntity;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the ocn repository API
 */
@Stateless
public class OcnRepo {
    @PersistenceContext(unitName = "ocnRepoPU")
    EntityManager entityManager;

    public OcnRepo() {}

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
        final List<WorldCatEntity> result = new ArrayList<>();
        if (value != null) {
            if (value.getPid() != null) {
                final WorldCatEntity entity = entityManager.find(WorldCatEntity.class, value.getPid());
                if (entity != null) {
                    result.add(entity);
                }
            } else if (value.getAgencyId() != null && value.getBibliographicRecordId() != null) {
                entityManager.createNamedQuery(WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME, WorldCatEntity.class)
                        .setParameter("agencyId", value.getAgencyId())
                        .setParameter("bibliographicRecordId", value.getBibliographicRecordId())
                        .getResultList()
                        .forEach(result::add);
            }
        }
        return result;
    }

    public List<String> pidListFromOcn(String ocn) {
        final List<String> pids = entityManager.createNamedQuery(
            WorldCatEntity.GET_PID_LIST_BY_OCN_QUERY_NAME, String.class)
            .setParameter("ocn", ocn).getResultList();
        return pids;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
