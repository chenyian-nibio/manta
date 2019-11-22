DROP TABLE IF EXISTS sample;
CREATE TABLE sample (
	id text PRIMARY KEY,
	create_date date
);

DROP TABLE IF EXISTS taxon_rank;
CREATE TABLE taxon_rank (
	id integer PRIMARY KEY,
	name text
);

DROP TABLE IF EXISTS taxonomy;
CREATE TABLE taxonomy (
	id text PRIMARY KEY,
	rank_id integer REFERENCES taxon_rank,
	name text
);

DROP TABLE IF EXISTS microbiota;
CREATE TABLE microbiota (
	sample_id text REFERENCES sample NOT NULL,
	taxonkey text,
	kingdom_id text REFERENCES taxonomy,
	phylum_id text REFERENCES taxonomy,
	class_id text REFERENCES taxonomy,
	order_id text REFERENCES taxonomy,
	family_id text REFERENCES taxonomy,
	genus_id text REFERENCES taxonomy,
	species_id text REFERENCES taxonomy,
	read_num integer,
	read_pct numeric
);

DROP TABLE IF EXISTS parameter_type;
CREATE TABLE parameter_type (
	id integer PRIMARY KEY,
	type_name text,
	description text
);

DROP TABLE IF EXISTS parameter_info;
CREATE TABLE parameter_info (
	sysid integer PRIMARY KEY AUTOINCREMENT NOT NULL,
	id text,
	title text,
	unit text,
	note text,
	type_id integer REFERENCES parameter_type,
	visible boolean
);

DROP TABLE IF EXISTS parameter_value;
CREATE TABLE parameter_value (
	sample_id text REFERENCES sample NOT NULL,
	parameter_id text REFERENCES parameter_info NOT NULL,
	parameter_value text
);

DROP TABLE IF EXISTS dominant_taxon;
CREATE TABLE dominant_taxon (
	sample_id text REFERENCES sample NOT NULL,
	rank_id integer REFERENCES taxon_rank NOT NULL,
	taxon_id text REFERENCES taxonomy NOT NULL
);

DROP TABLE IF EXISTS distance_type;
CREATE TABLE distance_type (
	id integer PRIMARY KEY,
	type_code text,
	type_name text
);

DROP TABLE IF EXISTS sample_distance;
CREATE TABLE sample_distance (
	sample_id_1 text REFERENCES sample NOT NULL,
	sample_id_2 text REFERENCES sample NOT NULL,
	distance numeric NOT NULL,
	distance_type_id integer REFERENCES distance_type NOT NULL
);

DROP TABLE IF EXISTS sample_diversity;
CREATE TABLE sample_diversity (
	sample_id text REFERENCES sample PRIMARY KEY,
	shannon numeric,
	simpson numeric
);

DROP TABLE IF EXISTS sample_display_columns;
CREATE TABLE sample_display_columns (
	position integer PRIMARY KEY,
	parameter_id text REFERENCES parameter_info
);

INSERT INTO parameter_type (id, type_name, description) VALUES (1, 'continuous', 'continuous variable');
INSERT INTO parameter_type (id, type_name, description) VALUES (2, 'category_u', 'unranked category');
INSERT INTO parameter_type (id, type_name, description) VALUES (3, 'category_r', 'ranked category');
INSERT INTO parameter_type (id, type_name, description) VALUES (4, 'text', 'free text');
INSERT INTO parameter_type (id, type_name, description) VALUES (5, 'others', 'others');

INSERT INTO taxon_rank (id, name) VALUES (1, 'kingdom');
INSERT INTO taxon_rank (id, name) VALUES (2, 'phylum');
INSERT INTO taxon_rank (id, name) VALUES (3, 'class');
INSERT INTO taxon_rank (id, name) VALUES (4, 'order');
INSERT INTO taxon_rank (id, name) VALUES (5, 'family');
INSERT INTO taxon_rank (id, name) VALUES (6, 'genus');
INSERT INTO taxon_rank (id, name) VALUES (7, 'species');

INSERT INTO sample_display_columns (position) VALUES (1);
INSERT INTO sample_display_columns (position) VALUES (2);
INSERT INTO sample_display_columns (position) VALUES (3);

INSERT INTO distance_type (id, type_code, type_name) VALUES (1, 'bray_genus', 'Bray-Curtis dissimilarity');
INSERT INTO distance_type (id, type_code, type_name) VALUES (2, 'jaccard', 'Jaccard distance');
