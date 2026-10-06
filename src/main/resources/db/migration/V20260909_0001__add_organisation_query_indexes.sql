-- Supports organisation-only /query pages ordered by role_name and id.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ra_org_role_name_id
    ON role_assignment (role_name, id)
    WHERE role_type = 'ORGANISATION';

-- Supports organisation-only /query pages filtered by role_category.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ra_org_category_role_name_id
    ON role_assignment (role_category, role_name, id)
    WHERE role_type = 'ORGANISATION';

-- Limits JSONB containment searches to organisation roles, avoiding CASE-role matches.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ra_org_attributes_gin
    ON role_assignment USING gin (attributes jsonb_path_ops)
    WHERE role_type = 'ORGANISATION';

-- Supports lean organisation-role sync grouped or paged by actor_id.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ra_org_actor_id_category
    ON role_assignment (actor_id) INCLUDE (role_category)
    WHERE role_type = 'ORGANISATION';
