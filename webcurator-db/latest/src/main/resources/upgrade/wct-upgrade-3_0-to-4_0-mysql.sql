-- Add unused tables
drop table if exists DB_WCT.ARC_HARVEST_FILE;
drop table if exists DB_WCT.ARC_HARVEST_RESOURCE;
drop table if exists DB_WCT.ARC_HARVEST_RESULT;
drop table if exists DB_WCT.HARVEST_RESOURCE;

--For visualization
create table DB_WCT.VISUALIZATION_IMPORTED_FILE (VIF_OID bigint, VIF_FILE_NAME varchar(1024) not null, VIF_CONTENT_LENGTH bigint, VIF_CONTENT_TYPE varchar(256), VIF_LAST_MODIFIED_DATE bigint, VIF_UPLOADED_DATE varchar(8), VIF_UPLOADED_TIME varchar(6), primary key (VIF_OID));
alter table DB_WCT.VISUALIZATION_IMPORTED_FILE add unique index IDX_VIF_FILE_NAME (VIF_FILE_NAME);

alter table DB_WCT.HARVEST_RESULT add column HR_STATUS integer;
