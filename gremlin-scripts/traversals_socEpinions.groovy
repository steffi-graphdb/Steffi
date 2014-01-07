l = []

/* The basic distributed traversal interface of Gremlin has the following parameters:
** P1: start vertex ID
** P2: Edge type (OUT | IN | UNDIRECTED)
** P3: end vertex ID
** P4: namePattern for the vertex
** P5: number of hops
**
** If you want to specify a traversal using the vertex properties you can use the
** native Imgraph interfaces of the package com.imgraph.traversal
*/
res = ImgraphTraversal.distributedTraversal('2276', 'OUT', '6743', null, 3)
l<<res.getTime()
sleep(1000)


res = ImgraphTraversal.distributedTraversal('1716', 'OUT', '29', null, 3)
l<<res.getTime()
sleep(1000)

res = ImgraphTraversal.distributedTraversal('7599', 'OUT', '6148', null, 3)
l<<res.getTime()
sleep(1000)

res = ImgraphTraversal.distributedTraversal('8021', 'OUT', '395', null, 3)
l<<res.getTime()
sleep(1000)

res = ImgraphTraversal.distributedTraversal('10002', 'OUT', '1', null, 3)
l<<res.getTime()
sleep(1000)

res = ImgraphTraversal.distributedTraversal('7499', 'OUT', '6348', null, 4)
l<<res.getTime()


print l
