--Remove the previous index as it has not been used
DROP INDEX idx_process_reference ON role_assignment_history USING btree (process, reference);
--Add new index with upper case process and reference combination
CREATE INDEX CONCURRENTLY idx_process_reference_upper ON role_assignment_history USING btree (upper(process), upper(reference));
