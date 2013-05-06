SELECT COUNT(*) AS cnt
 FROM t1
 JOIN t2 ON (    t1.c1 = t2.c10
             AND t1.c2 = t2.c9
             AND t1.c3 = t2.c8
             AND t1.c4 = t2.c7
             AND t1.c5 = t2.c6)
 JOIN t3 ON (    t1.c11 < t3.c20
             AND t1.c12 < t3.c19
             AND t1.c13 < t3.c18
             AND t1.c14 < t3.c17
             AND t1.c15 < t3.c16)
 JOIN t4 ON (    t1.c21 = t4.c30
             AND t1.c22 = t4.c29
             AND t1.c23 = t4.c28
             AND t1.c24 = t4.c27
             AND t1.c25 = t4.c26)
 JOIN t5 ON (    t1.c1 = t5.c10
             AND t1.c2 = t5.c9
             AND t1.c3 = t5.c8
             AND t1.c4 = t5.c7
             AND t1.c5 = t5.c6)
 JOIN t6 ON (    t1.c11 < t6.c20
             AND t1.c12 < t6.c19
             AND t1.c13 < t6.c18
             AND t1.c14 < t6.c17
             AND t1.c15 < t6.c16)
 JOIN t7 ON (    t1.c21 = t7.c30
             AND t1.c22 = t7.c29
             AND t1.c23 = t7.c28
             AND t1.c24 = t7.c27
             AND t1.c25 = t7.c26)
 JOIN t8 ON (    t1.c1 = t8.c10
             AND t1.c2 = t8.c9
             AND t1.c3 = t8.c8
             AND t1.c4 = t8.c7
             AND t1.c5 = t8.c6)
 JOIN t9 ON (    t1.c11 < t9.c20
             AND t1.c12 < t9.c19
             AND t1.c13 < t9.c18
             AND t1.c14 < t9.c17
             AND t1.c15 < t9.c16)
 JOIN t10 ON (    t1.c21 = t10.c30
              AND t1.c22 = t10.c29
              AND t1.c23 = t10.c28
              AND t1.c24 = t10.c27)