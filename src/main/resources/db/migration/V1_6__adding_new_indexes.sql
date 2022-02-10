--New Gin index on the attribute jsonb for any containment operator.
CREATE INDEX ON role_assignment USING gin(attributes jsonb_path_ops);

--New double index
CREATE INDEX idx_actor_role_type ON role_assignment USING btree (actor_id, role_type);

CREATE INDEX idx_actor_role_name ON role_assignment USING btree (actor_id, role_name);

CREATE INDEX idx_actor_attributes ON role_assignment USING btree (actor_id, attributes);

CREATE INDEX idx_attributes_role_type ON role_assignment USING btree (attributes, role_type);

CREATE INDEX idx_attributes_role_name ON role_assignment USING btree (attributes, role_name);

-- New Single Index
CREATE INDEX idx_role_type ON role_assignment USING btree (role_type);

CREATE INDEX idx_role_name ON role_assignment USING btree (role_name);

CREATE INDEX idx_classification ON role_assignment USING btree (classification);

CREATE INDEX idx_grant_type ON role_assignment USING btree (grant_type);

CREATE INDEX idx_role_category ON role_assignment USING btree (role_category);

CREATE INDEX idx_read_only ON role_assignment USING btree (read_only);

CREATE INDEX idx_begin_time ON role_assignment USING btree (begin_time);

CREATE INDEX idx_end_time ON role_assignment USING btree (end_time);

CREATE INDEX idx_created ON role_assignment USING btree (created);
