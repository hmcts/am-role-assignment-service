DELETE FROM role_assignment_history;
DELETE FROM role_assignment_request;
DELETE FROM role_assignment;
DELETE FROM actor_cache_control;


INSERT INTO public.role_assignment_request
(id, correlation_id, client_id, authenticated_user_id, assigner_id, request_type, status, process, reference, replace_existing, log, role_assignment_id, created)
VALUES('e1768fe8-f61d-4b56-99ec-9cc4c263b2c9', '6c57c405-b7b2-4851-a78c-a634adcc8ce1', 'ccd_gw', '6eb64a6f-8273-4cdf-9b72-0a0ae4f9444f', '123e4567-e89b-42d3-a456-556642445678', 'CREATE', 'APPROVED', 'S-052', 'S-052', false, 'Request has been validated by rule : R02_request_validation', NULL, '2020-07-26 23:39:13.683');

INSERT INTO public.role_assignment_history
(id, request_id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, status, reference, process, "attributes", notes, log, status_sequence, created,authorisations)
VALUES('f7edb29d-e421-450c-be66-a10169b04f0a', 'e1768fe8-f61d-4b56-99ec-9cc4c263b2c9', 'IDAM', '123e4567-e89b-42d3-a456-556642445612', 'CASE', 'salaried-judge', 'PUBLIC', 'SPECIFIC', 'JUDICIAL', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', 'CREATED', 'S-052', 'S-052', '{"caseId": "1234567890123456", "region": "south-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '[{"time": "2020-01-01T00:00", "userId": "003352d0-e699-48bc-b6f5-5810411e60af", "comment": "Need Access to case number 1234567890123456 for a year"}, {"time": "2020-01-02T00:00", "userId": "52aa3810-af1f-11ea-b3de-0242ac130004", "comment": "Access granted for 3 months"}]', NULL, 10, '2020-07-26 23:39:13.726',ARRAY['dev']);
INSERT INTO public.role_assignment_history
(id, request_id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, status, reference, process, "attributes", notes, log, status_sequence, created,authorisations)
VALUES('f7edb29d-e421-450c-be66-a10169b04f0a', 'e1768fe8-f61d-4b56-99ec-9cc4c263b2c9', 'IDAM', '123e4567-e89b-42d3-a456-556642445612', 'CASE', 'salaried-judge', 'PUBLIC', 'SPECIFIC', 'JUDICIAL', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', 'APPROVED', 'S-052', 'S-052', '{"caseId": "1234567890123456", "region": "south-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '[{"time": "2020-01-01T00:00", "userId": "003352d0-e699-48bc-b6f5-5810411e60af", "comment": "Need Access to case number 1234567890123456 for a year"}, {"time": "2020-01-02T00:00", "userId": "52aa3810-af1f-11ea-b3de-0242ac130004", "comment": "Access granted for 3 months"}]', 'Requested Role has been approved by rule : R12_role_validation_for_case_pattern ', 10, '2020-07-26 23:39:13.823',ARRAY['tester']);
INSERT INTO public.role_assignment_history
(id, request_id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, status, reference, process, "attributes", notes, log, status_sequence, created,authorisations)
VALUES('f7edb29d-e421-450c-be66-a10169b04f0a', 'e1768fe8-f61d-4b56-99ec-9cc4c263b2c9', 'IDAM', '123e4567-e89b-42d3-a456-556642445612', 'CASE', 'salaried-judge', 'PUBLIC', 'SPECIFIC', 'JUDICIAL', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', 'LIVE', 'S-052', 'S-052', '{"caseId": "1234567890123456", "region": "south-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '[{"time": "2020-01-01T00:00", "userId": "003352d0-e699-48bc-b6f5-5810411e60af", "comment": "Need Access to case number 1234567890123456 for a year"}, {"time": "2020-01-02T00:00", "userId": "52aa3810-af1f-11ea-b3de-0242ac130004", "comment": "Access granted for 3 months"}]', 'Requested Role has been approved by rule : R12_role_validation_for_case_pattern ', 10, '2020-07-26 23:39:13.871',ARRAY['tester']);

INSERT INTO public.role_assignment
(id, actor_id_type, actor_id, role_type, role_name, classification, grant_type, role_category, read_only, begin_time, end_time, "attributes", created)
VALUES('f7edb29d-e421-450c-be66-a10169b04f0a', 'IDAM', '123e4567-e89b-42d3-a456-556642445612', 'CASE', 'salaried-judge', 'PUBLIC', 'SPECIFIC', 'JUDICIAL', false, '2021-08-01 00:00:00.000', '2022-01-01 00:00:00.000', '{"caseId": "1234567890123456", "region": "south-east", "contractType": "SALARIED", "jurisdiction": "divorce"}', '2020-07-26 23:39:13.835');

INSERT INTO public.actor_cache_control
(actor_id, etag, json_response)
VALUES('123e4567-e89b-42d3-a456-556642445612', 0, '[{"id": "f7edb29d-e421-450c-be66-a10169b04f0a", "actorId": "123e4567-e89b-42d3-a456-556642445612", "created": {"hour": 23, "nano": 835139000, "year": 2020, "month": "JULY", "minute": 39, "second": 13, "dayOfWeek": "SUNDAY", "dayOfYear": 208, "chronology": {"id": "ISO", "calendarType": "iso8601"}, "dayOfMonth": 26, "monthValue": 7}, "endTime": {"hour": 0, "nano": 0, "year": 2022, "month": "JANUARY", "minute": 0, "second": 0, "dayOfWeek": "SATURDAY", "dayOfYear": 1, "chronology": {"id": "ISO", "calendarType": "iso8601"}, "dayOfMonth": 1, "monthValue": 1}, "readOnly": false, "roleName": "salaried-judge", "roleType": "CASE", "beginTime": {"hour": 0, "nano": 0, "year": 2021, "month": "AUGUST", "minute": 0, "second": 0, "dayOfWeek": "SUNDAY", "dayOfYear": 213, "chronology": {"id": "ISO", "calendarType": "iso8601"}, "dayOfMonth": 1, "monthValue": 8}, "grantType": "SPECIFIC", "attributes": {"caseId": "1234567890123456", "region": "south-east", "contractType": "SALARIED", "jurisdiction": "divorce"}, "actorIdType": "IDAM", "roleCategory": "JUDICIAL", "classification": "PUBLIC"}]');



