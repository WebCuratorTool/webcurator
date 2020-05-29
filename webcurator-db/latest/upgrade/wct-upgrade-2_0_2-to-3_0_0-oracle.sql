UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(ROL_OID) FROM DB_WCT.WCTROLE) WHERE IG_TYPE='Role';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(AGC_OID) FROM DB_WCT.AGENCY) WHERE IG_TYPE='Agency';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(USR_OID) FROM DB_WCT.WCTUSER) WHERE IG_TYPE='User';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(DC_OID) FROM DB_WCT.DUBLIN_CORE) WHERE IG_TYPE='DublinCore';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(PO_OID) FROM DB_WCT.PROFILE_OVERRIDES) WHERE IG_TYPE='PROFILE_OVERRIDE';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(RR_OID) FROM DB_WCT.REJECTION_REASON) WHERE IG_TYPE='RejectionReason';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(LOGDUR_OID) FROM DB_WCT.WCT_LOGON_DURATION) WHERE IG_TYPE='LogonDuration';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(BR_OID) FROM DB_WCT.BANDWIDTH_RESTRICTIONS) WHERE IG_TYPE='Bandwidth';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(PRV_OID) FROM DB_WCT.ROLE_PRIVILEGE) WHERE IG_TYPE='RolePriv';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(AN_OID) FROM DB_WCT.ANNOTATIONS) WHERE IG_TYPE='Annotation';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(TSK_OID) FROM DB_WCT.TASK) WHERE IG_TYPE='Task';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(AHF_OID) FROM DB_WCT.ARC_HARVEST_FILE) WHERE IG_TYPE='ArcHarvestFile';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(HRC_OID) FROM DB_WCT.HARVEST_RESOURCE) WHERE IG_TYPE='HarvestResource';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(AUD_OID) FROM DB_WCT.WCTAUDIT) WHERE IG_TYPE='Audit';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=(SELECT MAX(NOT_OID) FROM DB_WCT.NOTIFICATION) WHERE IG_TYPE='Notification';
UPDATE DB_WCT.ID_GENERATOR SET IG_VALUE=GREATEST(
	(SELECT MAX(TI_OID) FROM DB_WCT.TARGET_INSTANCE),
	(SELECT MAX(IC_OID) FROM DB_WCT.INDICATOR_CRITERIA),
	(SELECT MAX(HM_OID) FROM DB_WCT.HEATMAP_CONFIG),
	(SELECT MAX(S_OID) FROM DB_WCT.SCHEDULE),
	(SELECT MAX(AA_OID) FROM DB_WCT.AUTHORISING_AGENT),
	(SELECT MAX(UP_OID) FROM DB_WCT.URL_PATTERN),
	(SELECT MAX(AT_OID) FROM DB_WCT.ABSTRACT_TARGET),
	(SELECT MAX(I_OID) FROM DB_WCT.INDICATOR),
	(SELECT MAX(AT_OID) FROM DB_WCT.GROUP_MEMBER),
	(SELECT MAX(IRL_OID) FROM DB_WCT.INDICATOR_REPORT_LINE),
	(SELECT MAX(S_OID) FROM DB_WCT.SEED),
	(SELECT MAX(UPM_OID) FROM DB_WCT.URL_PERMISSION_MAPPING_VIEW),
	(SELECT MAX(F_OID) FROM DB_WCT.FLAG),
	(SELECT MAX(P_OID) FROM DB_WCT.PROFILE),
	(SELECT MAX(UPM_OID) FROM DB_WCT.URL_PERMISSION_MAPPING),
	(SELECT MAX(SH_OID) FROM DB_WCT.SEED_HISTORY),
	(SELECT MAX(ST_OID) FROM DB_WCT.SITE),
	(SELECT MAX(AT_OID) FROM DB_WCT.ABSTRACT_TARGET_GROUPTYPE_VIEW),
	(SELECT MAX(PE_OID) FROM DB_WCT.PERMISSION)
) WHERE IG_TYPE='General';
