--New Gin index on the attribute jsonb for any containment operator.
CREATE INDEX ON role_assignment USING gin(attributes jsonb_path_ops);
