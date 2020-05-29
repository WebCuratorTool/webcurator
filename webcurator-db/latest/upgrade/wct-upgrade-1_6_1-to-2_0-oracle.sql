-- PROFILE

alter table DB_WCT.PROFILE add (P_HARVESTER_TYPE varchar2(40));
update DB_WCT.PROFILE set P_HARVESTER_TYPE='HERITRIX1';
alter table DB_WCT.PROFILE modify P_HARVESTER_TYPE varchar2(40) not null;

alter table DB_WCT.PROFILE add (P_DATA_LIMIT_UNIT varchar2(40));
update DB_WCT.PROFILE set P_DATA_LIMIT_UNIT='B' where P_HARVESTER_TYPE='HERITRIX3';

alter table DB_WCT.PROFILE add (P_MAX_FILE_SIZE_UNIT varchar2(40));
update DB_WCT.PROFILE set P_MAX_FILE_SIZE_UNIT='B' where P_HARVESTER_TYPE='HERITRIX3';

alter table DB_WCT.PROFILE add (P_TIME_LIMIT_UNIT varchar2(40));
update DB_WCT.PROFILE set P_TIME_LIMIT_UNIT='SECOND' where P_HARVESTER_TYPE='HERITRIX3';

-- PROFILE_OVERRIDES

alter table DB_WCT.PROFILE_OVERRIDES add (PO_H3_DOC_LIMIT number(10,0), PO_H3_DATA_LIMIT double precision, PO_H3_DATA_LIMIT_UNIT varchar2(40),
PO_H3_TIME_LIMIT double precision, PO_H3_TIME_LIMIT_UNIT varchar2(40), PO_H3_MAX_PATH_DEPTH number(10,0), PO_H3_MAX_HOPS number(10,0),
PO_H3_MAX_TRANS_HOPS number(10,0), PO_H3_IGNORE_ROBOTS number(1,0), PO_H3_IGNORE_COOKIES number(1,0), PO_H3_OR_DOC_LIMIT number(1,0),
PO_H3_OR_DATA_LIMIT number(1,0), PO_H3_OR_TIME_LIMIT number(1,0), PO_H3_OR_MAX_PATH_DEPTH number(1,0), PO_H3_OR_MAX_HOPS number(1,0),
PO_H3_OR_MAX_TRANS_HOPS number(1,0), PO_H3_OR_IGNORE_ROBOTS number(1,0), PO_H3_OR_IGNORE_COOKIES number(1,0), PO_H3_OR_BLOCK_URL number(1,0),
PO_H3_OR_INCL_URL number(1,0));


-- Provide sensible defaults for new override attributes
update DB_WCT.PROFILE_OVERRIDES set
PO_H3_DATA_LIMIT_UNIT = 'B',
PO_H3_EXTRACT_JS = 1,
PO_H3_IGNORE_COOKIES = 0,
PO_H3_OR_EXTRACT_JS = 0,
PO_H3_OR_IGNORE_COOKIES = 0,
PO_H3_OR_MAX_TRANS_HOPS = 0,
PO_H3_OR_RAW_PROFILE = 0,
PO_H3_TIME_LIMIT_UNIT = 'SECOND',
PO_H3_DATA_LIMIT = PO_MAX_BYES,
PO_H3_DOC_LIMIT = PO_MAX_DOCS,
PO_H3_MAX_HOPS = PO_MAX_HOPS,
PO_H3_MAX_PATH_DEPTH = PO_MAX_PATH_DEPTH,
PO_H3_TIME_LIMIT = PO_MAX_TIME_SEC,
PO_H3_IGNORE_ROBOTS = case PO_ROBOTS_POLICY when 'ignore' then 1 else 0 end,
PO_H3_OR_BLOCK_URL = PO_OR_EXCLUSION_URI,
PO_H3_OR_INCL_URL = PO_OR_INCLUSION_URI,
PO_H3_OR_DATA_LIMIT = PO_OR_MAX_BYTES,
PO_H3_OR_DOC_LIMIT = PO_OR_MAX_DOCS,
PO_H3_OR_MAX_HOPS = PO_OR_MAX_HOPS,
PO_H3_OR_MAX_PATH_DEPTH = PO_OR_MAX_PATH_DEPTH,
PO_H3_OR_TIME_LIMIT = PO_OR_MAX_TIME_SEC,
PO_H3_OR_IGNORE_ROBOTS = PO_OR_ROBOTS_POLICY
;

create table DB_WCT.PO_H3_BLOCK_URL (PBU_PROF_OVER_OID number(19,0) not null, PBU_FILTER varchar2(255), PBU_IX number(10,0) not null, primary key (PBU_PROF_OVER_OID, PBU_IX));
create table DB_WCT.PO_H3_INCLUDE_URL (PIU_PROF_OVER_OID number(19,0) not null, PIU_FILTER varchar2(255), PIU_IX number(10,0) not null, primary key (PIU_PROF_OVER_OID, PIU_IX));

alter table DB_WCT.PO_H3_BLOCK_URL add constraint PBU_FK_1 foreign key (PBU_PROF_OVER_OID) references DB_WCT.PROFILE_OVERRIDES;
alter table DB_WCT.PO_H3_INCLUDE_URL add constraint PIU_FK_1 foreign key (PIU_PROF_OVER_OID) references DB_WCT.PROFILE_OVERRIDES;

GRANT SELECT, INSERT, UPDATE, DELETE ON DB_WCT.PO_H3_BLOCK_URL TO USR_WCT;
GRANT SELECT, INSERT, UPDATE, DELETE ON DB_WCT.PO_H3_INCLUDE_URL TO USR_WCT;

alter table DB_WCT.PROFILE add (P_IMPORTED number(1,0) default 0 not null);


alter table DB_WCT.PROFILE_OVERRIDES add (PO_H3_RAW_PROFILE clob);
alter table DB_WCT.PROFILE_OVERRIDES add (PO_H3_OR_RAW_PROFILE number(1,0));
update DB_WCT.PROFILE_OVERRIDES set PO_H3_OR_RAW_PROFILE = 0;
