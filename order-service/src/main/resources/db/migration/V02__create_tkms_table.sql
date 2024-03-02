CREATE TABLE outgoing_message_0_0 (
  id BIGSERIAL PRIMARY KEY,
  message BYTEA NOT NULL
) WITH (autovacuum_analyze_threshold=2000000000, toast_tuple_target=8160);