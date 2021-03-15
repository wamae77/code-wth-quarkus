CREATE TABLE image_files (
    id SERIAL PRIMARY KEY,
    property_id bigint,
    imageName VARCHAR(255),
    image_data bytea
 );

 ALTER TABLE property DROP COLUMN image_data;
