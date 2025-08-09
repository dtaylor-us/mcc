
-- If the table names differ (because of naming strategy), adjust accordingly.
-- Assuming entities map to "asset" and "work_log" by default conventions.

INSERT INTO asset (id, qr_code, name, model, serial_number, location, manual_path, installed_at)
VALUES (
           gen_random_uuid(),
           'QR-12345',
           'Air Handler - 7th Floor',
           'AH-900',
           'SN-0001',
           'Building A / Floor 7 / Mech Room',
           'file:/Users/dtaylor-us/projects/maintenance-control-console/manuals/air-handler.txt',
           NOW()
       );

-- Optional: more assets
-- INSERT INTO asset (...) VALUES (...);
