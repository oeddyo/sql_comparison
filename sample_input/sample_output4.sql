;             
CREATE USER IF NOT EXISTS SA SALT '1c0b944ad9b34258' HASH '59eb4c5f03ed072da041bf06af2f78da98bd7a361b8c24e4e46de668ee234bea' ADMIN;           
CREATE CACHED TABLE PUBLIC.T1(
    C1 INTEGER,
    C2 INTEGER,
    C3 INTEGER,
    C4 INTEGER,
    C5 INTEGER
);        
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.T1;      
CREATE CACHED TABLE PUBLIC.T2(
    A INTEGER,
    B INTEGER
);             
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.T2;      
INSERT INTO PUBLIC.T2(A, B) VALUES
(0, 0),
(0, 0);          
