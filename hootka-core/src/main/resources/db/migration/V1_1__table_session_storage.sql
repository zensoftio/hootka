CREATE TABLE session_storage
(
  id      VARCHAR PRIMARY KEY HASH,
  session VARCHAR NOT NULL
);
