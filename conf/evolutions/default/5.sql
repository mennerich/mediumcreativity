# DC schema

# --- !Ups


CREATE TABLE dimension (
    id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dimension varchar(64) NOT NULL
);

# --- !Downs

DROP TABLE dimension;