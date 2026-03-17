CREATE USER sandbox_user WITH PASSWORD 'sandriam8w9';
GRANT USAGE ON SCHEMA public TO sandbox_user;
REVOKE ALL ON SCHEMA public FROM sandbox_user;