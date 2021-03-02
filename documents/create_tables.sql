CREATE TABLE project (
	id integer PRIMARY KEY,
	code text,
	name text,
	name_jp text
);

CREATE TABLE sample (
	id text PRIMARY KEY,
	exp_date date,
	age integer,
	gender text,
	has_metadata boolean NOT NULL default false,
	has_16s boolean NOT NULL default false,
	has_shotgun boolean NOT NULL default false
);

CREATE TABLE sample_group (
	id integer PRIMARY KEY,
	group_id text NOT NULL,
	sample_id text REFERENCES sample NOT NULL,
	sample_role text
);

CREATE TABLE project_sample (
	project_id integer REFERENCES project NOT NULL,
	sample_id text REFERENCES sample NOT NULL
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

CREATE TABLE reference_db (
	id integer PRIMARY KEY,
	refdb_name text,
	website_url text
);

CREATE TABLE exp_method (
	id integer PRIMARY KEY,
	method_name text
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
	read_pct numeric,
	refdb_id integer REFERENCES reference_db,
	method_id integer REFERENCES exp_method
);

CREATE TABLE parameter_class (
	id integer PRIMARY KEY,
	class_code text,
	class_name text,
	class_name_jp text
);

CREATE TABLE parameter_category (
	id integer PRIMARY KEY,
	category_code text,
	category_name text,
	category_name_jp text,
	class_id integer REFERENCES parameter_class
);

CREATE TABLE parameter_group (
	id integer PRIMARY KEY,
	group_code text,
	group_name text,
	group_name_jp text,
	category_id integer REFERENCES parameter_category
);

CREATE TABLE parameter_type (
	id integer PRIMARY KEY,
	type_name text,
	description text,
	description_jp text
);

CREATE TABLE parameter_info (
	id text PRIMARY KEY,
	title text,
	title_jp text,
	unit text,
	unit_jp text,
	note text,
	note_jp text,
	group_id integer REFERENCES parameter_group,
	type_id integer REFERENCES parameter_type,
	visible boolean
);

CREATE TABLE parameter_value (
	sample_id text REFERENCES sample NOT NULL,
	parameter_id text REFERENCES parameter_info NOT NULL,
	parameter_value text,
	parameter_flag text, 
	PRIMARY KEY (sample_id, parameter_id)
);

CREATE TABLE choice (
	id integer PRIMARY KEY,
	parameter_id text REFERENCES parameter_info,
	choice_option text,
	choice_value text,
	choice_value_jp text
);

CREATE TABLE user_role (
	id integer PRIMARY KEY,
	user_role text
);

CREATE TABLE dbuser (
	id integer PRIMARY KEY,
	username text,
	password text,
	is_active boolean NOT NULL,
	role_id integer REFERENCES user_role,
	name text
);

CREATE TABLE project_privilege (
	user_id integer REFERENCES dbuser NOT NULL,
	project_id integer REFERENCES project NOT NULL
);

CREATE TABLE parameter_privilege (
	user_id integer REFERENCES dbuser NOT NULL,
	group_id integer REFERENCES parameter_group NOT NULL
);

CREATE TABLE dominant_taxon (
	sample_id text REFERENCES sample NOT NULL,
	rank_id integer REFERENCES taxon_rank NOT NULL,
	taxon_id text REFERENCES taxonomy NOT NULL,
	method_id integer REFERENCES exp_method
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
	distance_type_id integer REFERENCES distance_type NOT NULL,
	method_id integer REFERENCES exp_method
);

CREATE TABLE sample_diversity (
	sample_id text REFERENCES sample NOT NULL,
	shannon numeric,
	simpson numeric,
	chao1 integer,
	method_id integer REFERENCES exp_method,
    PRIMARY KEY (sample_id, method_id)
);

CREATE INDEX parameter_value_sample_id_idx ON parameter_value (sample_id);
CREATE INDEX sample_distance_idx ON sample_distance (sample_id_1, sample_id_2, distance_type_id, method_id);
CREATE INDEX sample_diversity_idx ON sample_diversity (sample_id, method_id);

CREATE TABLE sample_all_distance (
	sample_id text REFERENCES sample NOT NULL,
	all_distance text NOT NULL,
	distance_type_id integer REFERENCES distance_type NOT NULL,
	method_id integer REFERENCES exp_method
);
CREATE INDEX sample_all_distance_idx ON sample_all_distance (sample_id, distance_type_id, method_id);
