/*
Copyright Dansk Bibliotekscenter a/s. Licensed under GNU 3
See license text in LICENSE.txt
*/

ALTER TABLE worldcat ALTER COLUMN checksum TYPE TEXT;
ALTER TABLE worldcat ALTER COLUMN checksum DROP NOT NULL;
ALTER TABLE worldcat ALTER COLUMN checksum SET DEFAULT NULL;
