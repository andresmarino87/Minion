--DROP TABLES
DROP TABLE STATS_OSB_URI;
DROP TABLE STATS_OSB_WEBSERVICES;
DROP TABLE STATS_OSB_SERVICES;
--DROP TABLE STATS_OSB_FULL_SERVICES;
--DROP SEQUENCES
DROP SEQUENCE sou_seq;
DROP SEQUENCE sows_seq;
DROP SEQUENCE sos_seq;
--DROP SEQUENCES
DROP INDEX idx_sos_dc;
DROP INDEX idx_sou_dc;
DROP INDEX idx_sows_dc;
--DROP PROCEDURES
DROP PROCEDURE insertSTATS_OSB_URI;
DROP PROCEDURE insertSTATS_OSB_WEBSERVICES;
DROP PROCEDURE insertSTATS_OSB_SERVICES;
--DROP PROCEDURE insertStatsWithInterval;
COMMIT;
/