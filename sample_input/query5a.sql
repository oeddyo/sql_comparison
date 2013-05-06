SELECT t1.c1
FROM t1
WHERE t1.c2 > (SELECT MAX(t2.c2)
               FROM t2)
