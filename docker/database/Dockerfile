FROM postgres:11.15

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d role_assignment -U ${ROLE_ASSIGNMENT_DB_USERNAME}

EXPOSE 5432
