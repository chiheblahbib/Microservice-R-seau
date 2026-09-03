-- =====================================================================
--  Preparation d'un PostgreSQL LOCAL pour developper le service
--  reseau sans dependre du LAN du bureau.
--
--  A lancer une seule fois :
--    psql -h localhost -U postgres -f local-setup.sql
--  (psql demandera le mot de passe)
-- =====================================================================

-- Bases : memes noms qu'au bureau, pour que seul l'hote change dans la config
SELECT 'CREATE DATABASE "ARCEP-DEV"'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ARCEP-DEV') \gexec

SELECT 'CREATE DATABASE "ARCEP-AUDIT"'
 WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ARCEP-AUDIT') \gexec


-- ---------------------------------------------------------------------
--  Schemas de la base metier
--
--  Hibernate cree les TABLES (ddl-auto=update) mais jamais les SCHEMAS :
--  sans ces trois lignes, le demarrage echoue sur "schema does not exist".
--
--    reseau       -> les 7 entites propres au service
--    homologation -> les entites partagees (Client, Commentaire,
--                    Attestation), dont homologation reste proprietaire
--                    au bureau
--    public       -> le modele ACL et les tables ACT_* de Flowable
-- ---------------------------------------------------------------------
\connect "ARCEP-DEV"

CREATE SCHEMA IF NOT EXISTS reseau;
CREATE SCHEMA IF NOT EXISTS homologation;
-- public existe deja par defaut

-- kernel : une entite ACL (StateWorkflow -> k_e_pa_states) y est mappee
CREATE SCHEMA IF NOT EXISTS kernel;
-- audit : Envers ecrit ses revisions ici, dans la base PRIMAIRE (pas dans ARCEP-AUDIT)
CREATE SCHEMA IF NOT EXISTS audit;


-- ---------------------------------------------------------------------
--  Base d'audit : Envers y ecrit ses revisions
--  (spring.jpa.properties.org.hibernate.envers.default_schema=audit)
-- ---------------------------------------------------------------------
\connect "ARCEP-AUDIT"

CREATE SCHEMA IF NOT EXISTS audit;


\connect "ARCEP-DEV"
\echo ''
\echo '--- schemas de ARCEP-DEV ---'
SELECT nspname FROM pg_namespace
 WHERE nspname IN ('reseau', 'homologation', 'public', 'kernel', 'audit')
 ORDER BY nspname;
