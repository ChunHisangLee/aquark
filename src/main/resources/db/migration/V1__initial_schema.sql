DROP TABLE IF EXISTS sensor_data CASCADE;
CREATE TABLE sensor_data
(
    id       BIGSERIAL PRIMARY KEY,
    station_id     VARCHAR(50)      NOT NULL,
    obs_time TIMESTAMP NOT NULL,
    v1       DOUBLE PRECISION,
    v5       DOUBLE PRECISION,
    v6       DOUBLE PRECISION,
    rh       DOUBLE PRECISION,
    tx       DOUBLE PRECISION,
    echo     DOUBLE PRECISION,
    rain_d   DOUBLE PRECISION,
    speed    DOUBLE PRECISION
);


DROP TABLE IF EXISTS sensor_threshold CASCADE;
CREATE TABLE sensor_threshold
(
    id              BIGSERIAL PRIMARY KEY,
    sensor_name     VARCHAR(50)      NOT NULL,
    threshold_value DOUBLE PRECISION NOT NULL
);
