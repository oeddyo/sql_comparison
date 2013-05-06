SELECT *
FROM (
		SELECT c1, c2, c5, c6
		FROM t1
		UNION ALL
		SELECT c1, c2, c5, c6
		FROM t3
	 )
MINUS
     (
		SELECT c1, c2, c5, c6
		FROM t2
		UNION ALL
		SELECT c1, c2, c5, c6
		FROM t4
	 )