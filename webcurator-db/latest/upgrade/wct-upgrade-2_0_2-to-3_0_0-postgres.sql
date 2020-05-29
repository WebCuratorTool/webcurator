update db_wct.id_generator set ig_value=(select max(rol_oid) from db_wct.wctrole) where ig_type='Role';
update db_wct.id_generator set ig_value=(select max(agc_oid) from db_wct.agency) where ig_type='Agency';
update db_wct.id_generator set ig_value=(select max(usr_oid) from db_wct.wctuser) where ig_type='User';
update db_wct.id_generator set ig_value=(select max(dc_oid) from db_wct.dublin_core) where ig_type='DublinCore';
update db_wct.id_generator set ig_value=(select max(po_oid) from db_wct.profile_overrides) where ig_type='PROFILE_OVERRIDE';
update db_wct.id_generator set ig_value=(select max(rr_oid) from db_wct.rejection_reason) where ig_type='RejectionReason';
update db_wct.id_generator set ig_value=(select max(logdur_oid) from db_wct.wct_logon_duration) where ig_type='LogonDuration';
update db_wct.id_generator set ig_value=(select max(br_oid) from db_wct.bandwidth_restrictions) where ig_type='Bandwidth';
update db_wct.id_generator set ig_value=(select max(prv_oid) from db_wct.role_privilege) where ig_type='RolePriv';
update db_wct.id_generator set ig_value=(select max(an_oid) from db_wct.annotations) where ig_type='Annotation';
update db_wct.id_generator set ig_value=(select max(tsk_oid) from db_wct.task) where ig_type='Task';
update db_wct.id_generator set ig_value=(select max(ahf_oid) from db_wct.arc_harvest_file) where ig_type='ArcHarvestFile';
update db_wct.id_generator set ig_value=(select max(hrc_oid) from db_wct.harvest_resource) where ig_type='HarvestResource';
update db_wct.id_generator set ig_value=(select max(aud_oid) from db_wct.wctaudit) where ig_type='Audit';
update db_wct.id_generator set ig_value=(select max(not_oid) from db_wct.notification) where ig_type='Notification';
update db_wct.id_generator set ig_value=greatest(
	(select max(ti_oid) from db_wct.target_instance),
	(select max(ic_oid) from db_wct.indicator_criteria),
	(select max(hm_oid) from db_wct.heatmap_config),
	(select max(s_oid) from db_wct.schedule),
	(select max(aa_oid) from db_wct.authorising_agent),
	(select max(up_oid) from db_wct.url_pattern),
	(select max(at_oid) from db_wct.abstract_target),
	(select max(i_oid) from db_wct.indicator),
	(select max(at_oid) from db_wct.group_member),
	(select max(irl_oid) from db_wct.indicator_report_line),
	(select max(s_oid) from db_wct.seed),
	(select max(upm_oid) from db_wct.url_permission_mapping_view),
	(select max(f_oid) from db_wct.flag),
	(select max(p_oid) from db_wct.profile),
	(select max(upm_oid) from db_wct.url_permission_mapping),
	(select max(sh_oid) from db_wct.seed_history),
	(select max(st_oid) from db_wct.site),
	(select max(at_oid) from db_wct.abstract_target_grouptype_view),
	(select max(pe_oid) from db_wct.permission)
) where ig_type='General';
