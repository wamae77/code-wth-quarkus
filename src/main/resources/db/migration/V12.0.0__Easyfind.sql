ALTER TABLE image_files
DROP CONSTRAINT fk_property_id;
ALTER TABLE image_files ADD CONSTRAINT fk_property_id FOREIGN KEY(property_id) REFERENCES property(id)ON DELETE CASCADE ON UPDATE CASCADE;