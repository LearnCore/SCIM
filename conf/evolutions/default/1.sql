# Users schema

# --- !Ups

CREATE TABLE users (
  id varchar(36) NOT NULL,
  username varchar(256) NOT NULL,
  firstname varchar(256) NOT NULL,
  lastname varchar(256) NOT NULL,
  active tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE users;
