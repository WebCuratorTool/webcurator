-- Add unused tables
drop table if exists DB_WCT.ARC_HARVEST_FILE cascade;
drop table if exists DB_WCT.ARC_HARVEST_RESOURCE cascade;
drop table if exists DB_WCT.ARC_HARVEST_RESULT cascade;
drop table if exists DB_WCT.HARVEST_RESOURCE cascade;

create table DB_WCT.VISUALIZATION_IMPORTED_FILE (VIF_OID int8 not null, VIF_FILE_NAME varchar(1024) not null, VIF_CONTENT_LENGTH int8 not null, VIF_CONTENT_TYPE varchar(256), VIF_LAST_MODIFIED_DATE int8 not null, VIF_UPLOADED_DATE varchar(8), VIF_UPLOADED_TIME varchar(6), primary key (VIF_OID));
alter table DB_WCT.VISUALIZATION_IMPORTED_FILE add constraint IDX_VIF_FILE_NAME UNIQUE (VIF_FILE_NAME);


alter table DB_WCT.HARVEST_RESULT add column HR_STATUS int4;
