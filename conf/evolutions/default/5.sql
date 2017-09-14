# Groups test data

# --- !Ups

INSERT INTO groups
  (id)
VALUES
  ('78cd42ed-e5f8-4378-9332-67d44b23b0cf'),
  ('84919ed5-3465-40da-9f60-114b9e7a470b'),
  ('b7786ae9-27d6-4679-8ab5-5c84b2f72a7a');

# --- !Downs

DELETE FROM groups WHERE id IN (
  '78cd42ed-e5f8-4378-9332-67d44b23b0cf',
  '84919ed5-3465-40da-9f60-114b9e7a470b',
  'b7786ae9-27d6-4679-8ab5-5c84b2f72a7a'
);
