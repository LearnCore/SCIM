# Groups schema

# --- !Ups

CREATE TABLE users_groups (
  user_id varchar(36) NOT NULL,
  group_id varchar(36) NOT NULL,
  CONSTRAINT users_groups_users_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT users_groups_groups_fk FOREIGN KEY (group_id) REFERENCES groups (id),
  UNIQUE KEY user_id_group_id (user_id, group_id)
);

# --- !Downs

DROP TABLE users_groups;
