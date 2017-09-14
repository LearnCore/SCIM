# Groups schema

# --- !Ups

CREATE TABLE groups (
  id varchar(36) NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE groups;
