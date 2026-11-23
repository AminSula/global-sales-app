-- SOURCE tables: a stand-in for the client's existing database.
-- The app only READS these (the "Refresh from SQL" button copies them into product/customer).
-- When we connect to the client's real database, this script and V2 are dropped and the
-- reader is pointed at their real tables instead.

CREATE TABLE erp_products (
    code   VARCHAR(30)    NOT NULL PRIMARY KEY,
    name   VARCHAR(150)   NOT NULL,
    price  NUMERIC(12, 2) NOT NULL,
    stock  NUMERIC(12, 2) NOT NULL
);

CREATE TABLE erp_customers (
    code     VARCHAR(30)  NOT NULL PRIMARY KEY,
    name     VARCHAR(150) NOT NULL,
    address  VARCHAR(250)
);
