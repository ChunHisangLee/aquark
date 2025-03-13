-- 1. Sensor Data Table
DROP TABLE IF EXISTS sensor_data CASCADE;
CREATE TABLE sensor_data
(
    id         BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50),
    obs_time   TIMESTAMP NOT NULL,
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

-- 2. Alarm Threshold Table
DROP TABLE IF EXISTS alarm_threshold CASCADE;
CREATE TABLE alarm_threshold
(
    id              BIGSERIAL PRIMARY KEY,
    sensor_name     VARCHAR(50) UNIQUE NOT NULL,
    threshold_value DOUBLE PRECISION   NOT NULL
);

-- 3. Fetched API Table
DROP TABLE IF EXISTS fetched_api CASCADE;
CREATE TABLE fetched_api
(
    id         BIGSERIAL PRIMARY KEY,
    api_url    VARCHAR(255) UNIQUE NOT NULL,
    fetched_at TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Hourly Aggregation Table
DROP TABLE IF EXISTS hourly_aggregation CASCADE;
CREATE TABLE hourly_aggregation
(
    id          BIGSERIAL PRIMARY KEY,
    obs_date    DATE        NOT NULL,
    obs_hour    INTEGER     NOT NULL,
    sensor_name VARCHAR(50) NOT NULL,
    sum_value   DOUBLE PRECISION,
    avg_value   DOUBLE PRECISION
);

-- 5. Daily Aggregation Table
-- In this design, daily_aggregation will store 24 records per day (one for each hour)
DROP TABLE IF EXISTS daily_aggregation CASCADE;
CREATE TABLE daily_aggregation
(
    id          BIGSERIAL PRIMARY KEY,
    obs_date    DATE        NOT NULL,
    obs_hour    INTEGER     NOT NULL,
    sensor_name VARCHAR(50) NOT NULL,
    sum_value   DOUBLE PRECISION,
    avg_value   DOUBLE PRECISION,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
