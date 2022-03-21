CREATE TABLE auction_user
(
    user_id serial PRIMARY KEY,
    username VARCHAR ( 50 ) UNIQUE NOT NULL,
    password VARCHAR ( 50 ) NOT NULL,
    first_name VARCHAR (50) NOT NULL ,
    last_name VARCHAR (50) NOT NULL ,
    organisation_name VARCHAR (50) NOT NULL ,
    is_blocked BOOLEAN NOT NULL ,
    is_admin BOOLEAN NOT NULL  ,

);