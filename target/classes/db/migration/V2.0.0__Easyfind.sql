ALTER TABLE property ADD available BOOLEAN DEFAULT TRUE;
ALTER TABLE property ADD ownerId INT;
ALTER TABLE users ADD userrole VARCHAR(255);