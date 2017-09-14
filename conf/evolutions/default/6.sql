# Users-Groups test data

# --- !Ups

INSERT INTO users_groups
  (user_id, group_id)
VALUES
  -- george elliot
  ('1cda4679-3ed1-4439-b870-5132bbb286b2', '78cd42ed-e5f8-4378-9332-67d44b23b0cf'),
  ('1cda4679-3ed1-4439-b870-5132bbb286b2', '84919ed5-3465-40da-9f60-114b9e7a470b'),
  -- fyodor dostoevsky
  ('286d06ef-28ca-4037-9215-331012b397c5', 'b7786ae9-27d6-4679-8ab5-5c84b2f72a7a'),
  -- james levine
  ('3f03dd45-946a-483c-9da6-4e930189dcef', '84919ed5-3465-40da-9f60-114b9e7a470b'),
  ('3f03dd45-946a-483c-9da6-4e930189dcef', 'b7786ae9-27d6-4679-8ab5-5c84b2f72a7a')

# --- !Downs

TRUNCATE TABLE users_groups;
