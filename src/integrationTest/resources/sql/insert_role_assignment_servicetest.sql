DELETE FROM role_assignment;

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-ea4b1095eab1', 'IDAM', '123e4567-e89b-42d3-a456-556642445613', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--ActorID test
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-100000000001', 'IDAM', '1001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-100000000002', 'IDAM', '1002', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-100000000003', 'IDAM', '1002', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--RoleType Tests
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000001', 'IDAM', '2001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000002', 'IDAM', '2001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000003', 'IDAM', '2001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--RoleName test
--Classification PUBLIC, PRIVATE, RESTRICTED
--grantType BASIC,SPECIFIC, STANDARD, CHALLENGED, EXCLUDED
--RoleCategory JUDICIAL, LEGAL_OPERATIONS, ADMINISTRATION
--ValidAt
--RoleCategory JUDICIAL, LEGAL_OPERATIONS, ADMIN, PROFESSIONAL, CITIZEN, SYSTEM, OTHER_GOV_DEPT, CTSC
--Attributes
--hasAttributes
--ReadOnly
