CREATE TABLE asset (
    id UUID PRIMARY KEY,
    qr_code VARCHAR(255),
    name VARCHAR(255),
    model VARCHAR(255),
    serial_number VARCHAR(255),
    location VARCHAR(255),
    manual_path VARCHAR(255),
    installed_at TIMESTAMP
);
