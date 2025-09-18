-- SEQUENCE: public.employer_id_seq

-- DROP SEQUENCE IF EXISTS public.employer_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.employer_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.employer_id_seq
    OWNER TO postgres;

-- Index: by_hhid_emp

-- DROP INDEX IF EXISTS public.by_hhid_emp;

CREATE UNIQUE INDEX IF NOT EXISTS by_hhid_emp
    ON public.employer USING btree
    (hhid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: by_id_clustered_emp

-- DROP INDEX IF EXISTS public.by_id_clustered_emp;

CREATE UNIQUE INDEX IF NOT EXISTS by_id_clustered_emp
    ON public.employer USING btree
    (id ASC NULLS LAST)
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.employer
    CLUSTER ON by_id_clustered_emp;
	
-- SEQUENCE: public.project_id_seq

-- DROP SEQUENCE IF EXISTS public.project_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.project_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.project_id_seq
    OWNER TO postgres;
	-- SEQUENCE: public.project_search_text_id_seq

-- DROP SEQUENCE IF EXISTS public.project_search_text_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.project_search_text_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.project_search_text_id_seq
    OWNER TO postgres;
	-- SEQUENCE: public.vacancy_id_seq

-- DROP SEQUENCE IF EXISTS public.vacancy_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.vacancy_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.vacancy_id_seq
    OWNER TO postgres;
	
	
	
	
	-- Table: public.employer

-- DROP TABLE IF EXISTS public.employer;

CREATE TABLE IF NOT EXISTS public.employer
(
    id bigint NOT NULL DEFAULT nextval('employer_id_seq'::regclass),
    hhid text COLLATE pg_catalog."default",
    name text COLLATE pg_catalog."default",
    CONSTRAINT employer_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.employer
    OWNER to postgres;
	
	
	
	-- Table: public.project

-- DROP TABLE IF EXISTS public.project;

CREATE TABLE IF NOT EXISTS public.project
(
    id bigint NOT NULL DEFAULT nextval('project_id_seq'::regclass),
    name text COLLATE pg_catalog."default",
    CONSTRAINT project_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.project
    OWNER to postgres;
	
	
	
	-- Table: public.project_search_text

-- DROP TABLE IF EXISTS public.project_search_text;

CREATE TABLE IF NOT EXISTS public.project_search_text
(
    id bigint NOT NULL DEFAULT nextval('project_search_text_id_seq'::regclass),
    project_id bigint,
    name text COLLATE pg_catalog."default",
    CONSTRAINT project_search_text_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.project_search_text
    OWNER to postgres;
	
	
	
	
	-- Table: public.publication_history

-- DROP TABLE IF EXISTS public.publication_history;

CREATE TABLE IF NOT EXISTS public.publication_history
(
    date_published date,
    project_id bigint,
    vacancy_id bigint,
    hhid text COLLATE pg_catalog."default",
    date_closed date,
    logmoment timestamp without time zone
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.publication_history
    OWNER to postgres;
	
	
-- Table: public.search_history

-- DROP TABLE IF EXISTS public.search_history;

CREATE TABLE IF NOT EXISTS public.search_history
(
    search_date date,
    project_id bigint,
    vacancy_id bigint,
    hhid text COLLATE pg_catalog."default"
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.search_history
    OWNER to postgres;	
	
	
-- Table: public.vacancy

-- DROP TABLE IF EXISTS public.vacancy;

CREATE TABLE IF NOT EXISTS public.vacancy
(
    id bigint NOT NULL DEFAULT nextval('vacancy_id_seq'::regclass),
    hhid text COLLATE pg_catalog."default",
    name text COLLATE pg_catalog."default",
    employer_id bigint,
    salary_from numeric(20,0),
    salary_to numeric(20,0),
    gross smallint,
    url text COLLATE pg_catalog."default",
    alternate_url text COLLATE pg_catalog."default",
    archived smallint,
    CONSTRAINT vacancy_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.vacancy
    OWNER to postgres;

-- DROP INDEX IF EXISTS public.by_id_clustered;

CREATE UNIQUE INDEX IF NOT EXISTS by_id_clustered
    ON public.vacancy USING btree
    (id ASC NULLS LAST)
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.vacancy
    CLUSTER ON by_id_clustered;

-- DROP INDEX IF EXISTS public.by_hhid;

CREATE UNIQUE INDEX IF NOT EXISTS by_hhid
    ON public.vacancy USING btree
    (hhid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- SEQUENCE: public.vacancy_source_id_seq

-- DROP SEQUENCE IF EXISTS public.vacancy_source_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.vacancy_source_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.vacancy_source_id_seq
    OWNER TO postgres;


-- Table: public.vacancy_source

-- DROP TABLE IF EXISTS public.vacancy_source;

CREATE TABLE IF NOT EXISTS public.vacancy_source
(
    id bigint NOT NULL DEFAULT nextval('vacancy_source_id_seq'::regclass),
    hhid text COLLATE pg_catalog."default",
    json text COLLATE pg_catalog."default",
    CONSTRAINT vacancy_source_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.vacancy_source
    OWNER to postgres;
-- Index: by_hhid_source

-- DROP INDEX IF EXISTS public.by_hhid_source;

CREATE INDEX IF NOT EXISTS by_hhid_source
    ON public.vacancy_source USING btree
    (hhid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: by_id_clust_source

-- DROP INDEX IF EXISTS public.by_id_clust_source;

CREATE INDEX IF NOT EXISTS by_id_clust_source
    ON public.vacancy_source USING btree
    (id ASC NULLS LAST)
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.vacancy_source
    CLUSTER ON by_id_clust_source;