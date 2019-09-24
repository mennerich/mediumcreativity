# DC schema

# --- !Ups


CREATE TABLE work (
    id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title integer NOT NULL,
    description integer NOT NULL,
    creation_date date NOT NULL,
    available BOOLEAN NOT NULL
);


# --- !Downs

DROP TABLE work;