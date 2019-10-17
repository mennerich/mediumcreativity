# DC schema

# --- !Ups


CREATE TABLE work (
    id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title text NOT NULL,
    description text NOT NULL,
    creation_date date NOT NULL,
    available BOOLEAN NOT NULL,
    dimension_id integer
);


# --- !Downs

DROP TABLE work;