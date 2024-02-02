-- enable publiclaw_wa_1_2 flag in Demo for: DTSAM-95
update flag_config set status='true' where flag_name='publiclaw_wa_1_2' and env in ('demo');
