-- create table
CREATE TABLE flag_config(
	id bigint not null,
  flag_name text NOT NULL,
	env text NOT NULL,
	service_name text NOT NULL,
	status bool NOT NULL,
	CONSTRAINT flag_config_pkey PRIMARY KEY (id)
);
-- create sequence
create sequence ID_SEQ;
-- add sequence to table
ALTER TABLE flag_config ALTER COLUMN id
SET DEFAULT nextval('ID_SEQ');

-- insert iac base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_0', 'pr', 'ia', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_0', 'aat', 'ia', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_0', 'demo', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_0', 'perftest', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_0', 'ithc', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_0', 'prod', 'ia', 'false');

INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_1', 'pr', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_1', 'aat', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_1', 'demo', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_1', 'perftest', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_1', 'ithc', 'ia', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_drool_config_1_1', 'prod', 'ia', 'true');



