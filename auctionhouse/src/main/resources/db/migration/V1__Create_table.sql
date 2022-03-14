CREATE TABLE auction_user
(
    id           serial PRIMARY KEY,
    username     VARCHAR(50) UNIQUE NOT NULL,
    password     VARCHAR(50)        NOT NULL,
    first_name   VARCHAR(50)        NOT NULL,
    last_name    VARCHAR(50)        NOT NULL,
    organisation VARCHAR(50)        NOT NULL,
    blocked      BOOLEAN            NOT NULL,
    is_admin     BOOLEAN            NOT NULL

);

CREATE TABLE auction
(
    id    serial PRIMARY KEY,
    owner      VARCHAR(50)      NOT NULL  REFERENCES auction_user (username),
    min_price     DOUBLE PRECISION NOT NULL,
    quantity      BIGINT           NOT NULL,
    closing_time TIME,
    status        VARCHAR(50),
    symbol        VARCHAR(50),
    is_closed     BOOLEAN          NOT NULL DEFAULT FALSE

);


CREATE TABLE bids
(
    id serial REFERENCES auction ,
    owner      VARCHAR(50)      NOT NULL REFERENCES auction_user (username),
    price      DOUBLE PRECISION NOT NULL,
    quantity   BIGINT           NOT NULL,
    state  VARCHAR (50) NOT NULL ,
    win_quantity DOUBLE PRECISION NOT NULL

);

INSERT INTO auction_user (username, password, is_admin, first_name, last_name, organisation, blocked)
VALUES ('ADMIN', 'adminpassword', TRUE, 'admin', 'admin', 'Adaptive', FALSE);
