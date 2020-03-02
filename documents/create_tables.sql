CREATE TABLE project (
	id integer PRIMARY KEY,
	code text,
	name text,
	name_jp text
);

CREATE TABLE sample (
	id text PRIMARY KEY,
	subject_id text,
	exp_date date,
	age integer,
	gender integer
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
	refdb_id integer REFERENCES reference_db
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
	id integer PRIMARY KEY,
	db_code text,
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
	parameter_id integer REFERENCES parameter_info NOT NULL,
	parameter_value text,
	parameter_flag text
);

CREATE TABLE choice (
	id integer PRIMARY KEY,
	parameter_id integer REFERENCES parameter_info,
	choice_option text,
	choice_value text,
	choice_value_jp text
);

CREATE TABLE user_role (
	id integer PRIMARY KEY,
	user_role text
);

CREATE TABLE dbuser (
	id serial PRIMARY KEY,
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
