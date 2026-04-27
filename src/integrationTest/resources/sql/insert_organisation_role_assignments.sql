
-- role_category = ADMIN
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('88321fd6-3482-49ff-a141-bb71b1fd72db', 'IDAM', '42454e17-222b-4e91-8a6e-619654a0d361', 'ORGANISATION', 'hmcts-admin', 'PRIVATE', 'BASIC', 'ADMIN', true, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', '{}', '2020-07-24 15:05:01.988');

-- role_category = CTSC
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('25ad5b15-8569-483b-b890-1adb792a7fc9', 'IDAM', '22d33eaf-95a6-4856-b981-e8cde4ddda00', 'ORGANISATION', 'hmcts-ctsc', 'PUBLIC', 'SPECIFIC', 'CTSC', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', '{}', '2020-07-24 15:05:01.988');

-- role_category = LEGAL_OPERATIONS
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('df948472-0286-42f0-983c-25801923b4e8', 'IDAM', '420ae207-93a2-4677-99d1-8cf3996bfbfa', 'ORGANISATION', 'hmcts-legal-operations', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', '{}', '2020-07-24 15:05:01.988');

-- role_category = JUDICIAL
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('bd75d468-8b5f-47ec-aeac-823511e8b8f4', 'IDAM', 'ddf81529-5c67-4599-b4c5-40dcac04f8d2', 'ORGANISATION', 'hmcts-judiciary', 'PUBLIC', 'SPECIFIC', 'JUDICIAL', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', '{}', '2020-07-24 15:05:01.988');

-- role_category = PROFESSIONAL
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('cbe1e4f7-7964-4b78-b374-f612c43cac5c', 'IDAM', '90ca2538-920c-4719-9ef9-52230231c037', 'ORGANISATION', 'prm', 'PUBLIC', 'SPECIFIC', 'PROFESSIONAL', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', '{}', '2020-07-24 15:05:01.988');
