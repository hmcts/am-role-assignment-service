-- enable flags that are not enabled in AAT by flyway by default
update flag_config set status='true' where flag_name='iac_1_1' and env in ('aat');
update flag_config set status='true' where flag_name='iac_jrd_1_0' and env in ('aat');
update flag_config set status='true' where flag_name='iac_specific_1_0' and env in ('aat');
update flag_config set status='true' where flag_name='iac_challenged_1_0' and env in ('aat');
-- NB: 'ga_prm_1_0' currently only enabled in AAT
update flag_config set status='true' where flag_name='ga_prm_1_0' and env in ('aat');
