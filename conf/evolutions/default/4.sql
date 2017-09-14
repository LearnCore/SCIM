# Users test data

# --- !Ups

INSERT INTO users
  (id, username, firstname, lastname, active)
VALUES
	('1cda4679-3ed1-4439-b870-5132bbb286b2', 'gelliot', 'george', 'elliot', 1),
	('286d06ef-28ca-4037-9215-331012b397c5', 'fidofido', 'fyodor', 'dostoevsky', 1),
	('3f03dd45-946a-483c-9da6-4e930189dcef', 'jimbo123', 'james', 'levine', 1),
	('8ef2fc7d-709b-4949-96c7-0c910393ad77', 'anonymouse', 'john', 'doe', 1),
	('e38397c0-2f72-4238-8b15-c1477106c96e', 'vbeffa', 'vlad', 'beffa', 1);

# --- !Downs

DELETE FROM users WHERE id IN (
  '1cda4679-3ed1-4439-b870-5132bbb286b2',
  '286d06ef-28ca-4037-9215-331012b397c5',
  '3f03dd45-946a-483c-9da6-4e930189dcef',
  '8ef2fc7d-709b-4949-96c7-0c910393ad77',
  'e38397c0-2f72-4238-8b15-c1477106c96e'
);
