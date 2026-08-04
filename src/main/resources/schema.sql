CREATE UNIQUE INDEX IF NOT EXISTS uk_companies_name_active
    ON p_companies (name)
    WHERE deleted_at IS NULL;