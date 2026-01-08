DELETE FROM role_assignment;

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-ea4b1095eab1', 'IDAM', '123e4567-e89b-42d3-a456-556642445613', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--ActorID test
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-100000000001', 'IDAM', '1001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-100000000002', 'IDAM', '1002', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-100000000003', 'IDAM', '1003', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--RoleType Tests CASE, ORGANISATION
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000001', 'IDAM', '2001', 'ORGANISATION', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"roleType": "Test", "region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000002', 'IDAM', '2002', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"roleType": "Test", "region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-200000000003', 'IDAM', '2003', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"roleType": "Test","region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--RoleName Tests Solicitor , case-allocator,hearing-manager,hearing-viewer
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-300000000001', 'IDAM', '3001', 'CASE', 'Solicitor', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-300000000002', 'IDAM', '3002', 'CASE', 'hearing-manager', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-300000000003', 'IDAM', '3003', 'CASE', 'hearing-manager', 'PUBLIC', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--Classification Test PUBLIC, PRIVATE, RESTRICTED
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-400000000001', 'IDAM', '4001', 'CASE', 'judge', 'RESTRICTED', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-400000000002', 'IDAM', '4002', 'CASE', 'case-allocator', 'PRIVATE', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-400000000003', 'IDAM', '4003', 'CASE', 'case-allocator', 'PRIVATE', 'STANDARD', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--grantType Tests BASIC,SPECIFIC, STANDARD, CHALLENGED, EXCLUDED
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-500000000001', 'IDAM', '5001', 'CASE', 'judge', 'PUBLIC', 'CHALLENGED', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-500000000002', 'IDAM', '5002', 'CASE', 'case-allocator', 'PUBLIC', 'EXCLUDED', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-500000000003', 'IDAM', '5003', 'CASE', 'case-allocator', 'PUBLIC', 'EXCLUDED', 'PROFESSIONAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--RoleCategory Tests JUDICIAL, LEGAL_OPERATIONS, ADMIN, PROFESSIONAL, CITIZEN, SYSTEM, OTHER_GOV_DEPT, CTSC
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-600000000001', 'IDAM', '6001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'JUDICIAL', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-600000000002', 'IDAM', '6002', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'OTHER_GOV_DEPT', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-600000000003', 'IDAM', '6003', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'OTHER_GOV_DEPT', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

-- ValidAt Tests
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES
('638e8e7a-7d7c-4027-9d53-700000000001', 'IDAM', '7001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'LEGAL_OPERATIONS', false,
 NULL, NULL,
 '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}',
 now());

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES
('638e8e7a-7d7c-4027-9d53-700000000002', 'IDAM', '7002', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false,
 current_date -  10, NULL,
 '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}',
 now());

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES
('638e8e7a-7d7c-4027-9d53-700000000003', 'IDAM', '7003', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false,
 NULL, current_date + 10,
 '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}',
 now());

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES
('638e8e7a-7d7c-4027-9d53-700000000004', 'IDAM', '7004', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false,
 current_date + 9, current_date + 10,
 '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}',
 now());


--Attributes Tests
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-800000000001', 'IDAM', '8001', 'CASE', 'judge', 'PUBLIC', 'STANDARD', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "south-east", "contractType": "SALARIED", "caseType": "CT2", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-800000000002', 'IDAM', '8002', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "south-west", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-800000000003', 'IDAM', '8003', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "south-west", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--hasAttributes Tests
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-900000000001', 'IDAM', '9001', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"unique1": "123456789", "region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-900000000002', 'IDAM', '9002', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"unique2": "123456789", "region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--ReadOnly Tests
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-010000000001', 'IDAM', '0101', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', true, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('638e8e7a-7d7c-4027-9d53-010000000002', 'IDAM', '0101', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546');

--Authorisations Tests
INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created, authorisations)
VALUES('638e8e7a-7d7c-4027-9d53-110000000001', 'IDAM', '1101', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546', '{"auth1", "auth2"}');

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created, authorisations)
VALUES('638e8e7a-7d7c-4027-9d53-110000000002', 'IDAM', '1102', 'CASE', 'case-allocator', 'PUBLIC', 'SPECIFIC', 'LEGAL_OPERATIONS', false, '2021-01-01 12:00:00.000', current_date+5, '{"region": "north-east", "contractType": "SALARIED", "jurisdiction": "WA"}', '2020-06-24 17:35:08.546', '{"auth3", "auth4"}');

