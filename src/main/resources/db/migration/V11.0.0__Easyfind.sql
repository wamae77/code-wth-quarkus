ALTER TABLE image_files DROP COLUMN property_id;
ALTER TABLE image_files ADD COLUMN property_id int;
ALTER TABLE image_files ADD CONSTRAINT fk_property_id FOREIGN KEY(property_id) REFERENCES property(id);