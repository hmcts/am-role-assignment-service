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

--RoleType Tests CASE, ORGANISATION
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000001', 'IDAM', '2001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000002', 'IDAM', '2001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000003', 'IDAM', '2001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--RoleName Tests judge , case-allocator,hearing-manager,hearing-viewer
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-300000000001', 'IDAM', '3001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-300000000002', 'IDAM', '3001', 'CASE', 'case-allocator', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-300000000003', 'IDAM', '3001', 'CASE', 'case-allocator', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--Classification Test PUBLIC, PRIVATE, RESTRICTED
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-400000000001', 'IDAM', '4001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-400000000002', 'IDAM', '4001', 'CASE', 'case-allocator', 'PRIVATE', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-400000000003', 'IDAM', '4001', 'CASE', 'case-allocator', 'PRIVATE', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--grantType Tests BASIC,SPECIFIC, STANDARD, CHALLENGED, EXCLUDED
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-500000000001', 'IDAM', '5001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-500000000002', 'IDAM', '5001', 'CASE', 'case-allocator', 'PRIVATE', 'SPECIFIC', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-500000000003', 'IDAM', '5001', 'CASE', 'case-allocator', 'PRIVATE', 'SPECIFIC', NULL, false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-06-24 17:35:08.546');

--RoleCategory Test JUDICIAL, LEGAL_OPERATIONS, ADMINISTRATION
--ValidAt Tests
--RoleCategory Tests JUDICIAL, LEGAL_OPERATIONS, ADMIN, PROFESSIONAL, CITIZEN, SYSTEM, OTHER_GOV_DEPT, CTSC
--Attributes Tests
--hasAttributes Tests
--ReadOnly Tests
