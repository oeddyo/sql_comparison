SELECT t1.attr2
FROM tab1 t1
WHERE t1.attr1 IN (SELECT t2.attr1
                   FROM tab2 t2) 
                