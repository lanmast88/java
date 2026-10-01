TRUNCATE claims, policies, clients RESTART IDENTITY CASCADE;

INSERT INTO clients (last_name, first_name, phone) VALUES
    ('Соколов',    'Артём',  '+7 (915) 234-18-90'),
    ('Мельникова', 'Ольга',  '+7 (903) 771-42-05'),
    ('Гаврилов',   'Денис',  '+7 (926) 118-77-31'),
    ('Романова',   'Ксения', '+7 (964) 300-55-12'),
    ('Тихонов',    'Павел',  '+7 (999) 845-63-27');

-- premium = insured_sum * тариф типа: OSAGO 0.05, KASKO 0.08, DMS 0.04, PROPERTY 0.03.

INSERT INTO policies (number, client_id, type, status, insured_sum, premium, start_date, end_date) VALUES
    ('OSAGO-2026-0001',    (SELECT id FROM clients WHERE last_name = 'Соколов'),    'OSAGO',    'ACTIVE',     400000.00,   20000.00, '2026-03-01', '2027-03-01'),
    ('KASKO-2026-0002',    (SELECT id FROM clients WHERE last_name = 'Соколов'),    'KASKO',    'ACTIVE',    1250000.00,  100000.00, '2026-05-15', '2027-05-15'),
    ('DMS-2026-0003',      (SELECT id FROM clients WHERE last_name = 'Мельникова'), 'DMS',      'ACTIVE',     300000.00,   12000.00, '2026-01-10', '2027-01-10'),
    ('PROPERTY-2026-0004', (SELECT id FROM clients WHERE last_name = 'Гаврилов'),   'PROPERTY', 'ACTIVE',    2000000.00,   60000.00, '2026-06-01', '2027-06-01'),
    ('KASKO-2025-0005',    (SELECT id FROM clients WHERE last_name = 'Гаврилов'),   'KASKO',    'EXPIRED',    900000.00,   72000.00, '2025-02-01', '2026-02-01'),
    ('OSAGO-2026-0006',    (SELECT id FROM clients WHERE last_name = 'Романова'),   'OSAGO',    'ACTIVE',     400000.00,   20000.00, '2026-08-20', '2027-08-20'),
    ('DMS-2026-0007',      (SELECT id FROM clients WHERE last_name = 'Тихонов'),    'DMS',      'DRAFT',      500000.00,   20000.00, '2026-09-25', '2027-09-25'),
    ('PROPERTY-2026-0008', (SELECT id FROM clients WHERE last_name = 'Мельникова'), 'PROPERTY', 'CANCELLED',  750000.00,   22500.00, '2026-04-01', '2027-04-01');

-- payout > 0 только у убытков со статусом PAID: сумма проставляется при переходе APPROVED -> PAID.

INSERT INTO claims (policy_id, event_date, submitted_at, description, claimed_amount, payout, status) VALUES
    ((SELECT id FROM policies WHERE number = 'OSAGO-2026-0001'),    '2026-07-21', '2026-07-22', 'ДТП на перекрёстке, повреждён автомобиль третьего лица', 130000.00, 130000.00, 'PAID'),
    ((SELECT id FROM policies WHERE number = 'KASKO-2026-0002'),    '2026-06-12', '2026-06-14', 'Повреждение переднего бампера и фары на парковке',        85000.00,  85000.00, 'PAID'),
    ((SELECT id FROM policies WHERE number = 'KASKO-2026-0002'),    '2026-08-03', '2026-08-05', 'Скол лобового стекла от камня на трассе',                 42000.00,      0.00, 'REJECTED'),
    ((SELECT id FROM policies WHERE number = 'OSAGO-2026-0006'),    '2026-09-10', '2026-09-12', 'Наезд на препятствие, повреждён порог',                   61000.00,  45000.00, 'PAID'),
    ((SELECT id FROM policies WHERE number = 'DMS-2026-0003'),      '2026-09-02', '2026-09-03', 'Стационарное лечение после травмы колена',                96000.00,      0.00, 'APPROVED'),
    ((SELECT id FROM policies WHERE number = 'PROPERTY-2026-0004'), '2026-09-15', '2026-09-16', 'Залив квартиры из-за прорыва стояка',                    180000.00,      0.00, 'SUBMITTED');
