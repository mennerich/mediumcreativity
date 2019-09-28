# DC schema

# --- !Ups


CREATE TABLE image (
    id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
    work_id integer NOT NULL,
    version integer NOT NULL,
    uuid char(36) NOT NULL,
    creation_date date NOT NULL,
    available BOOLEAN NOT NULL
);


# --- !Downs

DROP TABLE image;