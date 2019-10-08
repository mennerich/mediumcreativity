# DC schema

# --- !Ups


CREATE TABLE session (
    id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session char(32) NOT NULL,
    user_id integer NOT NULL
);


# --- !Downs

DROP TABLE session;