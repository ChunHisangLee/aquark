DROP TABLE IF EXISTS sensor_data CASCADE;
CREATE TABLE sensor_data
(
    id         BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50),
    obs_time TIMESTAMP NOT NULL,
    csq        VARCHAR(20),

    v1         DOUBLE PRECISION,
    v2         DOUBLE PRECISION,
    v3         DOUBLE PRECISION,
    v4         DOUBLE PRECISION,
    v5         DOUBLE PRECISION,
    v6         DOUBLE PRECISION,
    v7         DOUBLE PRECISION,

    rh         DOUBLE PRECISION,
    tx         DOUBLE PRECISION,
    echo       DOUBLE PRECISION,
    rain_d     DOUBLE PRECISION,
    speed      DOUBLE PRECISION
);


DROP TABLE IF EXISTS alarm_threshold CASCADE;
CREATE TABLE alarm_threshold
(
    id              BIGSERIAL PRIMARY KEY,
    sensor_name     VARCHAR(50)      NOT NULL,
    threshold_value DOUBLE PRECISION NOT NULL
);


DROP TABLE IF EXISTS fetched_api CASCADE;
CREATE TABLE fetched_api
(
    id         BIGSERIAL PRIMARY KEY,
    api_url    VARCHAR(255) UNIQUE NOT NULL,
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


DROP TABLE IF EXISTS daily_aggregation CASCADE;
CREATE TABLE daily_aggregation
(
    id               BIGSERIAL PRIMARY KEY,
    obs_date         DATE        NOT NULL,
    sensor_name      VARCHAR(50) NOT NULL,
    sum_value        DOUBLE PRECISION,
    avg_hourly_value DOUBLE PRECISION,
    avg_daily_value  DOUBLE PRECISION,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
