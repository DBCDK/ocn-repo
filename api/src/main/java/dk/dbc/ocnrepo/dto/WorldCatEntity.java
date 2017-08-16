/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
 * See license text in LICENSE.txt
 */

package dk.dbc.ocnrepo.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@NamedQueries({
        @NamedQuery(name = WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME, query = WorldCatEntity.GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY),
        @NamedQuery(name = WorldCatEntity.GET_PID_LIST_BY_OCN_QUERY_NAME, query = WorldCatEntity.GET_PID_LIST_BY_OCN_QUERY)
})
@Table(name = "worldcat")
public class WorldCatEntity {
    public static final String GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY =
            "SELECT entity FROM WorldCatEntity entity WHERE entity.agencyId = :agencyId AND entity.bibliographicRecordId = :bibliographicRecordId";
    public static final String GET_PID_LIST_BY_OCN_QUERY =
        "SELECT entity.pid FROM WorldCatEntity entity WHERE entity.ocn = :ocn";
    public static final String GET_BY_AGENCYID_BIBLIOGRAPHICRECORDID_QUERY_NAME = "WorldCatEntity.getByAgencyIdBibliographicRecordId";
    public static final String GET_PID_LIST_BY_OCN_QUERY_NAME = "WorldCatEntity.getPidListByOcn";

    @Id
    private String pid;
    private String ocn;
    private String bibliographicRecordId;
    private Integer agencyId;
    private Integer checksum;

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

    public Integer getChecksum() {
        return checksum;
    }

    public WorldCatEntity withChecksum(Integer checksum) {
        this.checksum = checksum;
        return this;
    }

    @Override
    public String toString() {
        return "WorldCatEntity{" +
                "pid='" + pid + '\'' +
                ", ocn='" + ocn + '\'' +
                ", bibliographicRecordId='" + bibliographicRecordId + '\'' +
                ", agencyId=" + agencyId +
                ", checksum=" + checksum +
                '}';
    }
}
