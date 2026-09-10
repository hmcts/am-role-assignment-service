-- enable ga_prm_1_0 flag in Prod for: POFCC-405
update flag_config set status='true' where flag_name='ga_prm_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
