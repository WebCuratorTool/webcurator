alter table db_wct.harvest_result alter hr_index set default 0;
alter table db_wct.id_generator alter ig_value type bigint;
alter table DB_WCT.ARC_HARVEST_RESOURCE drop constraint FK6D84FEB12FF8F14B;
alter table DB_WCT.ARC_HARVEST_RESOURCE add constraint FK6D84FEB12FF8F14B foreign key (AHRC_HARVEST_RESOURCE_OID)
                                                        references DB_WCT.HARVEST_RESOURCE on delete cascade;

update db_wct.id_generator set ig_value=(COALESCE((select max(rol_oid) from db_wct.wctrole), 0)) where ig_type='Role';
update db_wct.id_generator set ig_value=(COALESCE((select max(agc_oid) from db_wct.agency), 0)) where ig_type='Agency';
update db_wct.id_generator set ig_value=(COALESCE((select max(usr_oid) from db_wct.wctuser), 0)) where ig_type='User';
update db_wct.id_generator set ig_value=(COALESCE((select max(dc_oid) from db_wct.dublin_core), 0)) where ig_type='DublinCore';
update db_wct.id_generator set ig_value=(COALESCE((select max(po_oid) from db_wct.profile_overrides), 0)) where ig_type='PROFILE_OVERRIDE';
update db_wct.id_generator set ig_value=(COALESCE((select max(rr_oid) from db_wct.rejection_reason), 0)) where ig_type='RejectionReason';
update db_wct.id_generator set ig_value=(COALESCE((select max(logdur_oid) from db_wct.wct_logon_duration), 0)) where ig_type='LogonDuration';
update db_wct.id_generator set ig_value=(COALESCE((select max(br_oid) from db_wct.bandwidth_restrictions), 0)) where ig_type='Bandwidth';
update db_wct.id_generator set ig_value=(COALESCE((select max(prv_oid) from db_wct.role_privilege), 0)) where ig_type='RolePriv';
update db_wct.id_generator set ig_value=(COALESCE((select max(an_oid) from db_wct.annotations), 0)) where ig_type='Annotation';
update db_wct.id_generator set ig_value=(COALESCE((select max(tsk_oid) from db_wct.task), 0)) where ig_type='Task';
update db_wct.id_generator set ig_value=(COALESCE((select max(ahf_oid) from db_wct.arc_harvest_file), 0)) where ig_type='ArcHarvestFile';
update db_wct.id_generator set ig_value=(COALESCE((select max(hrc_oid) from db_wct.harvest_resource), 0)) where ig_type='HarvestResource';
update db_wct.id_generator set ig_value=(COALESCE((select max(aud_oid) from db_wct.wctaudit), 0)) where ig_type='Audit';
update db_wct.id_generator set ig_value=(COALESCE((select max(not_oid) from db_wct.notification), 0)) where ig_type='Notification';
update db_wct.id_generator set ig_value=greatest(
	(COALESCE((select max(ti_oid) from db_wct.target_instance), 0)),
	(COALESCE((select max(ic_oid) from db_wct.indicator_criteria), 0)),
	(COALESCE((select max(hm_oid) from db_wct.heatmap_config), 0)),
	(COALESCE((select max(s_oid) from db_wct.schedule), 0)),
	(COALESCE((select max(aa_oid) from db_wct.authorising_agent), 0)),
	(COALESCE((select max(up_oid) from db_wct.url_pattern), 0)),
	(COALESCE((select max(at_oid) from db_wct.abstract_target), 0)),
	(COALESCE((select max(i_oid) from db_wct.indicator), 0)),
	(COALESCE((select max(at_oid) from db_wct.group_member), 0)),
	(COALESCE((select max(irl_oid) from db_wct.indicator_report_line), 0)),
	(COALESCE((select max(s_oid) from db_wct.seed), 0)),
	(COALESCE((select max(upm_oid) from db_wct.url_permission_mapping_view), 0)),
	(COALESCE((select max(f_oid) from db_wct.flag), 0)),
	(COALESCE((select max(p_oid) from db_wct.profile), 0)),
	(COALESCE((select max(upm_oid) from db_wct.url_permission_mapping), 0)),
	(COALESCE((select max(sh_oid) from db_wct.seed_history), 0)),
	(COALESCE((select max(st_oid) from db_wct.site), 0)),
	(COALESCE((select max(at_oid) from db_wct.abstract_target_grouptype_view), 0)),
	(COALESCE((select max(pe_oid) from db_wct.permission), 0))
) where ig_type='General';

