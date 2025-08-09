-- Create databases
CREATE DATABASE assetdb;
CREATE DATABASE agentdb;

-- Create users if they don't exist
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'asset') THEN
    CREATE USER asset WITH PASSWORD 'asset';
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'agent') THEN
    CREATE USER agent WITH PASSWORD 'agent';
  END IF;
END $$;

-- Give ownership of assetdb to asset user
ALTER DATABASE assetdb OWNER TO asset;

-- Give ownership of agentdb to agent user
ALTER DATABASE agentdb OWNER TO agent;

-- Connect to assetdb and set schema permissions
\connect assetdb

-- Make asset own the public schema
ALTER SCHEMA public OWNER TO asset;

-- Allow asset to create objects in the public schema
GRANT USAGE, CREATE ON SCHEMA public TO asset;

-- Remove wide-open privileges from the public role (optional hardening)
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- Ensure asset uses the public schema by default
ALTER ROLE asset IN DATABASE assetdb SET search_path = public;

-- Connect to agentdb and set schema permissions
\connect agentdb

ALTER SCHEMA public OWNER TO agent;
GRANT USAGE, CREATE ON SCHEMA public TO agent;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
ALTER ROLE agent IN DATABASE agentdb SET search_path = public;
