-- insert fr_wa_1_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'local', 'fr', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'pr', 'fr', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'aat', 'fr', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'demo', 'fr', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'perftest', 'fr', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'ithc', 'fr', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('fr_wa_1_0', 'prod', 'fr', 'false');
