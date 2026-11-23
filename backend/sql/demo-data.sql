-- DEMO DATA for testing the app by hand (PostgreSQL).
--
-- How to run: pgAdmin -> database sales_pilot -> Query Tool -> open this file -> Execute (F5).
-- Run it AFTER the backend has started at least once (that creates the tables and the 3 accounts).
-- It is safe to run more than once: rows that already exist are skipped.
-- All dates are relative to TODAY, so the assignments are always "for today".
--
-- What you get:
--   * products and customers copied from the source tables (same as pressing "Rifresko nga SQL")
--   * 4 offers: two active, one expired, one paused
--   * visit lists for both agents for yesterday, today and tomorrow
--   * 12 orders over the last week, some approved and some pending
--
-- NOT part of the delivery to the client. To remove everything it added, see the end of this file.

BEGIN;

-- ---------- 1. Products and customers (same result as "Rifresko nga SQL") ----------

INSERT INTO product (code, name, price, stock, active, synced_at)
SELECT code, name, price, stock, TRUE, LOCALTIMESTAMP FROM erp_products
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name, price = EXCLUDED.price, stock = EXCLUDED.stock,
        active = TRUE, synced_at = EXCLUDED.synced_at;

INSERT INTO customer (code, name, address, active, synced_at)
SELECT code, name, address, TRUE, LOCALTIMESTAMP FROM erp_customers
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name, address = EXCLUDED.address,
        active = TRUE, synced_at = EXCLUDED.synced_at;

-- ---------- 2. Offers ----------

CREATE TEMP TABLE demo_offer (
    name VARCHAR(100), pct NUMERIC(5, 2), all_customers BOOLEAN,
    start_offset INT, end_offset INT, enabled BOOLEAN,
    product_codes TEXT[], customer_codes TEXT[]
) ON COMMIT DROP;

INSERT INTO demo_offer VALUES
    ('Zbritje vjeshte',      10.00, TRUE,  -14,   16, TRUE,  ARRAY['P007', 'P008', 'P009'], ARRAY[]::TEXT[]),
    ('Oferta për baret',      7.50, FALSE, NULL, NULL, TRUE,  ARRAY['P004', 'P005', 'P006'], ARRAY['K004', 'K009']),
    ('Java e detergjentëve', 15.00, TRUE,  -20,   -6, TRUE,  ARRAY['P019', 'P020'],         ARRAY[]::TEXT[]),
    ('Oferta e bazës',        5.00, TRUE,  NULL, NULL, FALSE, ARRAY['P010', 'P011', 'P012'], ARRAY[]::TEXT[]);

INSERT INTO offer (name, discount_percent, all_customers, start_date, end_date, enabled, created_at)
SELECT d.name, d.pct, d.all_customers,
       CURRENT_DATE + d.start_offset, CURRENT_DATE + d.end_offset, d.enabled, LOCALTIMESTAMP
FROM demo_offer d
WHERE NOT EXISTS (SELECT 1 FROM offer o WHERE o.name = d.name);

INSERT INTO offer_product (offer_id, product_id)
SELECT o.id, p.id
FROM demo_offer d
JOIN offer o ON o.name = d.name
JOIN product p ON p.code = ANY (d.product_codes)
ON CONFLICT DO NOTHING;

INSERT INTO offer_customer (offer_id, customer_id)
SELECT o.id, c.id
FROM demo_offer d
JOIN offer o ON o.name = d.name
JOIN customer c ON c.code = ANY (d.customer_codes)
ON CONFLICT DO NOTHING;

-- ---------- 3. Visit lists (yesterday, today, tomorrow) ----------

CREATE TEMP TABLE demo_visit (username VARCHAR(50), day_offset INT, visit_order INT, customer_code VARCHAR(30))
    ON COMMIT DROP;

INSERT INTO demo_visit VALUES
    ('agent1', -1, 1, 'K002'), ('agent1', -1, 2, 'K005'), ('agent1', -1, 3, 'K010'),
    ('agent2', -1, 1, 'K006'), ('agent2', -1, 2, 'K008'),
    ('agent1',  0, 1, 'K001'), ('agent1',  0, 2, 'K003'), ('agent1',  0, 3, 'K004'), ('agent1', 0, 4, 'K010'),
    ('agent2',  0, 1, 'K006'), ('agent2',  0, 2, 'K007'), ('agent2',  0, 3, 'K009'),
    ('agent1',  1, 1, 'K002'), ('agent1',  1, 2, 'K005'),
    ('agent2',  1, 1, 'K008'), ('agent2',  1, 2, 'K009');

-- A customer already assigned on that day (to anyone) is left as it is.
INSERT INTO visit_assignment (agent_id, customer_id, visit_date, visit_order)
SELECT u.id, c.id, CURRENT_DATE + d.day_offset, d.visit_order
FROM demo_visit d
JOIN app_user u ON u.username = d.username
JOIN customer c ON c.code = d.customer_code
ON CONFLICT (customer_id, visit_date) DO NOTHING;

-- ---------- 4. Orders ----------

CREATE TEMP TABLE demo_order (
    request_id VARCHAR(36), username VARCHAR(50), customer_code VARCHAR(30),
    days_ago INT, at_time TIME, approved BOOLEAN
) ON COMMIT DROP;

INSERT INTO demo_order VALUES
    ('demo-0001', 'agent1', 'K001', 6, '09:15', TRUE),
    ('demo-0002', 'agent2', 'K006', 6, '10:40', TRUE),
    ('demo-0003', 'agent1', 'K003', 5, '11:05', TRUE),
    ('demo-0004', 'agent2', 'K009', 4, '09:50', TRUE),
    ('demo-0005', 'agent1', 'K004', 4, '13:20', TRUE),
    ('demo-0006', 'agent2', 'K007', 3, '10:10', TRUE),
    ('demo-0007', 'agent1', 'K010', 2, '12:30', TRUE),
    ('demo-0008', 'agent2', 'K008', 2, '15:45', FALSE),
    ('demo-0009', 'agent1', 'K002', 1, '09:30', TRUE),
    ('demo-0010', 'agent1', 'K005', 1, '11:15', FALSE),
    ('demo-0011', 'agent2', 'K006', 1, '14:00', FALSE),
    ('demo-0012', 'agent1', 'K001', 0, '08:45', FALSE);

-- pct / offer_name say which offer applied when the order was "taken" (0 / NULL = none).
CREATE TEMP TABLE demo_line (
    request_id VARCHAR(36), line_no INT, product_code VARCHAR(30),
    quantity NUMERIC(12, 2), pct NUMERIC(5, 2), offer_name VARCHAR(100)
) ON COMMIT DROP;

INSERT INTO demo_line VALUES
    ('demo-0001', 1, 'P001', 10, 0, NULL), ('demo-0001', 2, 'P010', 20, 0, NULL), ('demo-0001', 3, 'P013', 30, 0, NULL),
    ('demo-0002', 1, 'P002', 12, 0, NULL), ('demo-0002', 2, 'P008', 2, 10, 'Zbritje vjeshte'),
    ('demo-0003', 1, 'P014', 24, 0, NULL), ('demo-0003', 2, 'P011', 40, 0, NULL), ('demo-0003', 3, 'P007', 6, 10, 'Zbritje vjeshte'),
    ('demo-0004', 1, 'P004', 18, 7.5, 'Oferta për baret'), ('demo-0004', 2, 'P006', 48, 7.5, 'Oferta për baret'), ('demo-0004', 3, 'P015', 4, 0, NULL),
    ('demo-0005', 1, 'P005', 12, 7.5, 'Oferta për baret'), ('demo-0005', 2, 'P008', 3, 10, 'Zbritje vjeshte'), ('demo-0005', 3, 'P003', 10, 0, NULL),
    ('demo-0006', 1, 'P001', 30, 0, NULL), ('demo-0006', 2, 'P017', 24, 0, NULL), ('demo-0006', 3, 'P018', 24, 0, NULL),
    ('demo-0007', 1, 'P012', 15, 0, NULL), ('demo-0007', 2, 'P016', 12, 0, NULL),
    ('demo-0008', 1, 'P009', 10, 10, 'Zbritje vjeshte'), ('demo-0008', 2, 'P010', 25, 0, NULL),
    ('demo-0009', 1, 'P013', 50, 0, NULL), ('demo-0009', 2, 'P014', 12, 0, NULL), ('demo-0009', 3, 'P019', 3, 0, NULL),
    ('demo-0010', 1, 'P007', 8, 10, 'Zbritje vjeshte'), ('demo-0010', 2, 'P002', 6, 0, NULL),
    ('demo-0011', 1, 'P006', 24, 0, NULL), ('demo-0011', 2, 'P017', 12, 0, NULL), ('demo-0011', 3, 'P001', 8, 0, NULL),
    ('demo-0012', 1, 'P008', 1, 10, 'Zbritje vjeshte'), ('demo-0012', 2, 'P011', 20, 0, NULL);

-- Headers first (total and final number are filled in below).
INSERT INTO sales_order (order_number, client_request_id, customer_id, agent_id, created_at, total,
                         status, approved_at, approved_by)
SELECT d.request_id, d.request_id, c.id, u.id,
       (CURRENT_DATE - d.days_ago) + d.at_time,
       0,
       CASE WHEN d.approved THEN 'APPROVED' ELSE 'PENDING' END,
       CASE WHEN d.approved THEN (CURRENT_DATE - d.days_ago) + d.at_time + INTERVAL '2 hours' END,
       CASE WHEN d.approved THEN (SELECT id FROM app_user WHERE username = 'admin') END
FROM demo_order d
JOIN app_user u ON u.username = d.username
JOIN customer c ON c.code = d.customer_code
ON CONFLICT (client_request_id) DO NOTHING;

-- Lines, with the same formula the application uses:
--   line total = quantity x price x (100 - discount %) / 100, rounded to 2 decimals
INSERT INTO sales_order_line (order_id, line_no, product_id, product_code, product_name,
                              quantity, unit_price, discount_percent, offer_name, line_total)
SELECT o.id, l.line_no, p.id, p.code, p.name,
       l.quantity, p.price, l.pct, l.offer_name,
       ROUND(l.quantity * p.price * (100 - l.pct) / 100, 2)
FROM demo_line l
JOIN sales_order o ON o.client_request_id = l.request_id
JOIN product p ON p.code = l.product_code
ON CONFLICT (order_id, line_no) DO NOTHING;

UPDATE sales_order o
SET total = (SELECT COALESCE(SUM(l.line_total), 0) FROM sales_order_line l WHERE l.order_id = o.id),
    order_number = 'POR-' || LPAD(o.id::TEXT, 6, '0')
WHERE o.client_request_id LIKE 'demo-%';

COMMIT;

-- ---------- To remove the demo ORDERS again (optional) ----------
-- DELETE FROM sales_order_line WHERE order_id IN (SELECT id FROM sales_order WHERE client_request_id LIKE 'demo-%');
-- DELETE FROM sales_order WHERE client_request_id LIKE 'demo-%';
