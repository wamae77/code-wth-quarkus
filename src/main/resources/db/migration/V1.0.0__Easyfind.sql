CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    fName VARCHAR(255),
    LName VARCHAR(255),
    email VARCHAR(255),
    phoneNumber VARCHAR(255),
    password VARCHAR (255),
    verified BOOLEAN DEFAULT FALSE
 );

CREATE TABLE property (
    id SERIAL PRIMARY KEY,
    propertyname VARCHAR(255),
    location VARCHAR(255),
    price VARCHAR(255),
    description VARCHAR(255),
    numberofrooms VARCHAR(255),
    s3Url VARCHAR(255)
);