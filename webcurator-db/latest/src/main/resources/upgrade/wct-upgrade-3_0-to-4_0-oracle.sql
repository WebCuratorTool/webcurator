-- Add unused tables
drop table DB_WCT.ARC_HARVEST_FILE cascade constraints;
drop table DB_WCT.ARC_HARVEST_RESOURCE cascade constraints;
drop table DB_WCT.ARC_HARVEST_RESULT cascade constraints;
drop table DB_WCT.HARVEST_RESOURCE cascade constraints;

create table DB_WCT.VISUALIZATION_IMPORTED_FILE (VIF_OID number(19,0) not null, VIF_FILE_NAME varchar(1024) not null, VIF_CONTENT_LENGTH number(10,0), VIF_CONTENT_TYPE varchar(256), VIF_LAST_MODIFIED_DATE number(13,0), VIF_UPLOADED_DATE varchar(8), VIF_UPLOADED_TIME varchar(6), primary key (VIF_OID));
alter table DB_WCT.VISUALIZATION_IMPORTED_FILE add constraint IDX_VIF_FILE_NAME UNIQUE (VIF_FILE_NAME);

alter table DB_WCT.HARVEST_RESULT add (HR_STATUS number(10,0));
