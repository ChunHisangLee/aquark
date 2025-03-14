-- 1. Sensor Data Table
DROP TABLE IF EXISTS sensor_data CASCADE;
CREATE TABLE sensor_data
(
    id         BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50) NOT NULL,
    obs_time   TIMESTAMP NOT NULL,
    csq        VARCHAR(20) NOT NULL,
    v1         NUMERIC(19,4),
    v2         NUMERIC(19,4),
    v3         NUMERIC(19,4),
    v4         NUMERIC(19,4),
    v5         NUMERIC(19,4),
    v6         NUMERIC(19,4),
    v7         NUMERIC(19,4),
    rh         NUMERIC(19,4),
    tx         NUMERIC(19,4),
    echo       NUMERIC(19,4),
    rain_d     NUMERIC(19,4),
    speed      NUMERIC(19,4),
    UNIQUE (station_id, obs_time, csq)
);

-- 2. Alarm Threshold Table
DROP TABLE IF EXISTS alarm_threshold CASCADE;
CREATE TABLE alarm_threshold
(
    id              BIGSERIAL PRIMARY KEY,
    station_id      VARCHAR(50) NOT NULL,
    csq             VARCHAR(20) NOT NULL,
    parameter       VARCHAR(50) NOT NULL,
    threshold_value NUMERIC(19,4) NOT NULL,
    UNIQUE (station_id, csq, parameter)
);

-- 3. Hourly Aggregation Table
DROP TABLE IF EXISTS hourly_aggregation CASCADE;
CREATE TABLE hourly_aggregation
(
    id           BIGSERIAL PRIMARY KEY,
    station_id   VARCHAR(50) NOT NULL,
    obs_date     DATE NOT NULL,
    obs_hour     INTEGER NOT NULL,
    csq          VARCHAR(20) NOT NULL,
    sensor_name  VARCHAR(50) NOT NULL,
    sum_value    NUMERIC(19,4),
    avg_value    NUMERIC(19,4),
    UNIQUE (station_id, obs_date, obs_hour, csq, sensor_name)
);

-- 4. Daily Aggregation Table
DROP TABLE IF EXISTS daily_aggregation CASCADE;
CREATE TABLE daily_aggregation
(
    id           BIGSERIAL PRIMARY KEY,
    station_id   VARCHAR(50) NOT NULL,
    obs_date     DATE NOT NULL,
    csq          VARCHAR(20) NOT NULL,
    sensor_name  VARCHAR(50) NOT NULL,
    sum_value    NUMERIC(19,4),
    avg_value    NUMERIC(19,4),
    UNIQUE (station_id, obs_date, csq, sensor_name)
);

-- 5. Temporary Sensor Data Table
DROP TABLE IF EXISTS temp_sensor_data CASCADE;
CREATE TABLE temp_sensor_data
(
    id         BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(50) NOT NULL,
    obs_time   TIMESTAMP NOT NULL,
    csq        VARCHAR(20) NOT NULL,
    v1         NUMERIC(19,4),
    v2         NUMERIC(19,4),
    v3         NUMERIC(19,4),
    v4         NUMERIC(19,4),
    v5         NUMERIC(19,4),
    v6         NUMERIC(19,4),
    v7         NUMERIC(19,4),
    rh         NUMERIC(19,4),
    tx         NUMERIC(19,4),
    echo       NUMERIC(19,4),
    rain_d     NUMERIC(19,4),
    speed      NUMERIC(19,4),
    UNIQUE (station_id, obs_time, csq)
);
