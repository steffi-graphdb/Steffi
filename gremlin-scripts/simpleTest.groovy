//Script that creates a small graph indexing vertex and edge properties
g.registerItemName("weight")
g.registerItemName("age")
g.registerItemName("degree")
g.startTransaction()
i = g.createIndex("vIndex", ImgraphVertex)
j = g.createIndex("eIndex", ImgraphEdge)
v = g.addVertex(1)
w = g.addVertex(2)
z = g.addVertex(3)
e = g.addEdge(null, v, w, null)
f = g.addEdge(null, z, w, null)
h = g.addEdge(null, w, z, null)
v["weight"] = 40
w["weight"] = 40
z["weight"] = 10
v["age"] = 5
w["age"] = 6
z["age"] = 1
i.put("weight", 40, v)
i.put("weight", 40, w)
i.put("weight", 10, z)
i.put("age", 5, v)
i.put("age", 6, w)
i.put("age", 1, z)
e["degree"] = "High"
f["degree"] = "Low"
h["degree"] = "High"
j.put("degree", "High", e)
j.put("degree", "Low", f)
j.put("degree", "High", h)
g.commit()
