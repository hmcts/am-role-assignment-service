-- AM-2521: Remove old unused DB flags for IAC

delete from flag_config where flag_name='iac_1_1';
delete from flag_config where flag_name='iac_specific_1_0';
delete from flag_config where flag_name='iac_challenged_1_0';
delete from flag_config where flag_name='iac_jrd_1_0';
