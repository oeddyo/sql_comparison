SELECT t1.c1 AS t1c1,
       t1.c2 AS t1c2,
       t1.c3 AS t1c3,
       t2.c1 AS t2c1,
       t2.c2 AS t2c2,
       t2.c3 AS t2c3,
       t2.c4 AS t2c4,
       t2.c5 AS t2c5,
       t2.c6 AS t2c6,
       t2.c7 AS t2c7,
       t2.c8 AS t2c8,
       t2.c9 AS t2c9,
       t2.c10 AS t2c10,
       t2.c11 AS t2c11,
       t2.c12 AS t2c12
FROM t1, t2
WHERE t1.c1 = t2.c1
  AND t1.c1 = t2.c2
  AND t1.c1 = t2.c3
  AND t1.c1 = t2.c4
  AND t1.c1 = t2.c5
  AND t1.c2 = t2.c11
  AND t1.c2 = t2.c12
  AND t1.c3 = t2.c9
  AND t1.c3 <> t2.c6
  AND t2.c7 <> t2.c8