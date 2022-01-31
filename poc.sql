CREATE TABLE source_code (
    id               BIGSERIAL,
    source_code      VARCHAR(128) NOT NULL,
    mid13            VARCHAR(128) NOT NULL,
    mid10            VARCHAR(128) NOT NULL,
    first2           VARCHAR(128) NOT NULL,
    activity_type    VARCHAR(128) NOT NULL,
    activity_source  VARCHAR(128) NOT NULL,
    activity_source2 VARCHAR(128) NOT NULL,
    activity_source3 VARCHAR(128) NOT NULL,
    activity_source4 VARCHAR(128) NOT NULL,
    activity_source5 VARCHAR(128) NOT NULL,
    campaign_no      VARCHAR(128) NULL,
    advertised_rate  REAL NOT NULL,
    brand_code       VARCHAR(128) NOT NULL,
    pct              VARCHAR(128) NOT NULL,
    end2             VARCHAR(128) NOT NULL,
    PRIMARY KEY(id)
);

CREATE INDEX idx_source_code ON source_code(source_code);

CREATE TABLE lookup_ (
    brand        VARCHAR(128) NOT NULL,
    option_value VARCHAR(128) NOT NULL
);

