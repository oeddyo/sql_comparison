CREATE TABLE t1 (
  c1  DOUBLE,
  c2  DOUBLE,
  c3  VARCHAR(10),
  c4  INTEGER,
  c5  DOUBLE,
  c6  DOUBLE,
  CONSTRAINT pk_t1_c1_c2 PRIMARY KEY (c1, c2),
  CONSTRAINT uk_t1_c3_c4 UNIQUE (c3, c4)
 );

CREATE TABLE t2 (
  c1  DOUBLE,
  c2  DOUBLE,
  c3  VARCHAR(10),
  c4  INTEGER,
  c5  DOUBLE,
  c6  DOUBLE,
  CONSTRAINT pk_t2_c1_c2 PRIMARY KEY (c1, c2),
  CONSTRAINT uk_t2_c3_c5 UNIQUE (c3, c5)
 );

CREATE TABLE t3 (
  c1  DOUBLE,
  c2  DOUBLE,
  c3  VARCHAR(10),
  c4  INTEGER,
  c5  DOUBLE,
  c6  DOUBLE,
  CONSTRAINT pk_t3_c1_c2 PRIMARY KEY (c1, c2),
  CONSTRAINT uk_t3_c3_c6 UNIQUE (c3, c6)
 );

CREATE TABLE t4 (
  c1  DOUBLE,
  c2  DOUBLE,
  c3  VARCHAR(10),
  c4  INTEGER,
  c5  DOUBLE,
  c6  DOUBLE,
  CONSTRAINT pk_t4_c1_c2 PRIMARY KEY (c1, c2),
  CONSTRAINT uk_t4_c4_c5 UNIQUE (c4, c5)
 );

 