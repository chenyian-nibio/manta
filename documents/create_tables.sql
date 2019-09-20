CREATE TABLE sample (
	id text PRIMARY KEY,
	create_date date
);

CREATE TABLE taxon_rank (
	id integer PRIMARY KEY,
	name text
);

CREATE TABLE taxonomy (
	id text PRIMARY KEY,
	rank_id integer REFERENCES taxon_rank,
	name text
);

CREATE TABLE microbiota (
	sample_id text REFERENCES sample NOT NULL,
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

CREATE TABLE parameter_type (
	id integer PRIMARY KEY,
	type_name text,
	description text
);

CREATE TABLE parameter_info (
	sysid SERIAL NOT NULL,
	id text PRIMARY KEY,
	title text,
	unit text,
	note text,
	type_id integer REFERENCES parameter_type,
	visible boolean
);

CREATE TABLE parameter_value (
	sample_id text REFERENCES sample NOT NULL,
	parameter_id integer REFERENCES parameter_info NOT NULL,
	parameter_value text,
	parameter_flag text
);

CREATE TABLE dominant_taxon (
	sample_id text REFERENCES sample NOT NULL,
	rank_id integer REFERENCES taxon_rank NOT NULL,
	taxon_id text REFERENCES taxonomy NOT NULL
);

CREATE TABLE distance_type (
	id integer PRIMARY KEY,
	type_code text,
	type_name text
);

CREATE TABLE sample_distance (
	sample_id_1 text REFERENCES sample NOT NULL,
	sample_id_2 text REFERENCES sample NOT NULL,
	distance numeric NOT NULL,
	distance_type_id integer REFERENCES distance_type NOT NULL
);

CREATE TABLE sample_diversity (
	sample_id text REFERENCES sample PRIMARY KEY,
	shannon numeric,
	simpson numeric,
	chao1 integer
);

CREATE TABLE sample_display_columns (
	sysid SERIAL NOT NULL,
	position integer PRIMARY KEY,
	parameter_id integer REFERENCES parameter_info NOT NULL
);
