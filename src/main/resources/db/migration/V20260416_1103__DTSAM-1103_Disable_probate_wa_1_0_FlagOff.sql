-- TEMP disable disposer_1_1 flag in PREVIEW for testing of DTSAM-1103
update flag_config set status='false' where flag_name='disposer_1_1' and env in ('pr');
