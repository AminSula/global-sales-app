-- Demo data for the SOURCE tables: 20 products and 10 customers.

INSERT INTO erp_products (code, name, price, stock) VALUES
    ('P001', 'Ujë natyral 0.5L (pako 12)',        360.00, 240),
    ('P002', 'Ujë natyral 1.5L (pako 6)',         300.00, 180),
    ('P003', 'Ujë me gaz 0.75L (pako 6)',         420.00,  90),
    ('P004', 'Lëng portokalli 1L',                180.00, 150),
    ('P005', 'Lëng pjeshke 1L',                   180.00, 120),
    ('P006', 'Pije energjike 250ml',              150.00, 300),
    ('P007', 'Kafe e bluar 250g',                 390.00,  75),
    ('P008', 'Kafe ekspres kokërr 1kg',          1650.00,  40),
    ('P009', 'Çaj mali 50g',                      220.00,  60),
    ('P010', 'Sheqer 1kg',                        130.00, 400),
    ('P011', 'Miell tip 00 1kg',                  110.00, 350),
    ('P012', 'Oriz 1kg',                          210.00, 200),
    ('P013', 'Makarona spageti 500g',              95.00, 500),
    ('P014', 'Vaj luledielli 1L',                 280.00, 160),
    ('P015', 'Vaj ulliri ekstra i virgjër 0.75L', 950.00,  55),
    ('P016', 'Salcë domatesh 500g',               140.00, 130),
    ('P017', 'Biskota me çokollatë 150g',         120.00, 220),
    ('P018', 'Çokollatë me qumësht 100g',         160.00, 180),
    ('P019', 'Detergjent rrobash 3kg',            890.00,  45),
    ('P020', 'Letër higjienike (pako 10)',        450.00,   0);

INSERT INTO erp_customers (code, name, address) VALUES
    ('K001', 'Market Besa',          'Rr. e Kavajës 112, Tiranë'),
    ('K002', 'Minimarket Arbri',     'Rr. Myslym Shyri 45, Tiranë'),
    ('K003', 'Supermarket Drini',    'Bul. Zogu I 78, Tiranë'),
    ('K004', 'Bar Kafe Liria',       'Rr. e Durrësit 203, Tiranë'),
    ('K005', 'Market Fjona',         'Rr. Bardhyl 17, Tiranë'),
    ('K006', 'Dyqan Ushqimor Era',   'Rr. Aleksandër Goga 9, Durrës'),
    ('K007', 'Market Plazhi',        'Rr. Pavarësia 150, Durrës'),
    ('K008', 'Minimarket Kruja',     'Rr. Kastriotët 4, Krujë'),
    ('K009', 'Restorant Shkëmbi',    'Rr. Kombëtare, Fushë-Krujë'),
    ('K010', 'Market Vllazërimi',    'Rr. Ferit Xhajko 31, Tiranë');
