# DC schema

# --- !Ups


CREATE TABLE user (
    id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email varchar(64) NOT NULL,
    salt varchar(10) NOT NULL,
    hash varchar(32) NOT NULL,
    nick varchar(16),
    is_admin BOOLEAN
);


# --- !Downs

DROP TABLE user;