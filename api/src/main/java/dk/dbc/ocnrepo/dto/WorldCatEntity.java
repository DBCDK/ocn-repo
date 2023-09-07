/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo.dto;

import dk.dbc.commons.jpa.converter.StringListToPgTextArrayConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME, query = WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY),
        @NamedQuery(name = WorldCatEntity.GET_PID_LIST_BY_OCN_QUERY_NAME, query = WorldCatEntity.GET_PID_LIST_BY_OCN_QUERY),
        @NamedQuery(name = WorldCatEntity.GET_OCN_BY_PID_QUERY_NAME, query = WorldCatEntity.GET_OCN_BY_PID_QUERY),
        @NamedQuery(name = WorldCatEntity.GET_ENTITIES_WITH_LHR_QUERY_NAME, query = WorldCatEntity.GET_ENTITIES_WITH_LHR_QUERY)
})
@Table(name = "worldcat")
public class WorldCatEntity {
    public static final String GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY =
            "SELECT entity FROM WorldCatEntity entity WHERE entity.agencyId = :agencyId AND entity.bibliographicRecordId = :bibliographicRecordId";
    public static final String GET_PID_LIST_BY_OCN_QUERY =
        "SELECT entity.pid FROM WorldCatEntity entity WHERE entity.ocn = :ocn";
    public static final String GET_OCN_BY_PID_QUERY =
        "SELECT entity.ocn FROM WorldCatEntity entity WHERE entity.pid = :pid";
    public static final String GET_ENTITIES_WITH_LHR_QUERY =
        "SELECT entity FROM WorldCatEntity entity WHERE entity.hasLHR = TRUE";
    public static final String GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME = "WorldCatEntity.getByAgencyIdBibliographicRecordId";
    public static final String GET_PID_LIST_BY_OCN_QUERY_NAME = "WorldCatEntity.getPidListByOcn";
    public static final String GET_OCN_BY_PID_QUERY_NAME = "WorldCatEntity.getOcnByPid";
    public static final String GET_ENTITIES_WITH_LHR_QUERY_NAME = "WorldCatEntity.getEntitiesWithLHR";

    @Id
    private String pid;
    private String ocn;
    private String bibliographicRecordId;
    private String checksum;
    private Integer agencyId;
    private boolean hasLHR;

    @Column(updatable = false)
    private Timestamp created;
    private Timestamp modified;

    @Convert(converter = StringListToPgTextArrayConverter.class)
    private List<String> activeHoldingSymbols;

    public String getPid() {
        return pid;
    }

    public WorldCatEntity withPid(String pid) {
        this.pid = pid;
        return this;
    }

    public String getOcn() {
        return ocn;
    }

    public WorldCatEntity withOcn(String ocn) {
        this.ocn = ocn;
        return this;
    }

    public String getBibliographicRecordId() {
        return bibliographicRecordId;
    }

    public WorldCatEntity withBibliographicRecordId(String bibliographicRecordId) {
        this.bibliographicRecordId = bibliographicRecordId;
        return this;
    }

    public Integer getAgencyId() {
        return agencyId;
    }

    public WorldCatEntity withAgencyId(Integer agencyId) {
        this.agencyId = agencyId;
        return this;
    }

    public String getChecksum() {
        return checksum;
    }

    public WorldCatEntity withChecksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    public boolean hasLHR() {
        return hasLHR;
    }

    public WorldCatEntity setHasLHR(boolean hasLHR) {
        this.hasLHR = hasLHR;
        return this;
    }

    public List<String> getActiveHoldingSymbols() {
        if (activeHoldingSymbols != null) {
            return new ArrayList<>(activeHoldingSymbols);
        }
        return Collections.emptyList();
    }

    public WorldCatEntity withActiveHoldingSymbols(List<String> activeHoldingSymbols) {
        if (activeHoldingSymbols != null) {
            this.activeHoldingSymbols = new ArrayList<>(activeHoldingSymbols);
        } else {
            this.activeHoldingSymbols = null;
        }
        return this;
    }

    @PrePersist
    public void setCreated() {
        created = modified = new Timestamp(System.currentTimeMillis());
    }

    public Instant getCreated() {
        if (created != null) {
            return created.toInstant();
        }
        return null;
    }

    @PreUpdate
    public void setModified() {
        modified = new Timestamp(System.currentTimeMillis());
    }

    public Instant getModified() {
        if (modified != null) {
            return modified.toInstant();
        }
        return null;
    }

    @Override
    public String toString() {
        return "WorldCatEntity{" +
                "pid='" + pid + '\'' +
                ", ocn='" + ocn + '\'' +
                ", bibliographicRecordId='" + bibliographicRecordId + '\'' +
                ", checksum='" + checksum + '\'' +
                ", agencyId=" + agencyId +
                ", hasLHR=" + hasLHR +
                ", created=" + created +
                ", modified=" + modified +
                ", activeHoldingSymbols=" + activeHoldingSymbols +
                '}';
    }
}
