/*
Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
See license text in LICENSE.txt
*/

CREATE TABLE worldcat (
  pid                     TEXT PRIMARY KEY,
  ocn                     TEXT NOT NULL,
  agencyId                INTEGER NOT NULL,
  bibliographicRecordId   TEXT NOT NULL
);

CREATE INDEX worldcat_agencyId_bibliographicRecordId_index ON worldcat(agencyId, bibliographicRecordId);
