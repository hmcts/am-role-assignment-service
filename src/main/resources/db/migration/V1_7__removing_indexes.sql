--Remove all the indexes to unblock the migration failure

DROP INDEX CONCURRENTLY idx_actor_role_type, idx_actor_role_name, idx_actor_attributes, idx_attributes_role_type,
idx_attributes_role_name, idx_role_type, idx_role_name, idx_classification, idx_grant_type, idx_role_category, idx_read_only,
idx_begin_time, idx_end_time, idx_created, role_assignment_attributes_idx;

