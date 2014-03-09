# Welcome to STEFFI

STEFFI is a distributed graph database fully in-memory and amazingly fast when it comes to querying large datasets.

As a scalable graph database, STEFFI's performance can directly be compared to Neo4j and Titan (http://steffi.io/#results). It provides its users with a clear competitive advantage when it comes to complicated traversal operations on large datasets. Speedups of up to 200 have been observed when comparing STEFFI with its alternatives.

More than an alternative to existing solutions, STEFFI opens up new possibilities for high-performance graph storage and manipulation.

## Getting and Using Steffi
Feel free to look at the Steffi Github wiki for detailed information.
The installation guide (https://github.com/steffi-graphdb/Steffi/wiki/Installation-guide) provides instructions to build the Steffi JAR, the Gremlin console and the Rexster server. Then we describe how to start Steffi and to use gremlin for loading and querying graph on the Using Steffi page (https://github.com/steffi-graphdb/Steffi/wiki/Using-Steffi).
Steffi also comes with a set of examples:
- neo4J-tests/ : The Maven project used to run the Neo4J experiments described on the thesis
- titan-tests/ : The Maven project used to run the Titan experiments described on the thesis
- msc/ : Example files referenced on the BUILD-INSTRUCTIONS.txt file
- gremlin-scripts/ : Gremlin scripts showing simple examples of the use of Steffi


There is a set of unit tests in the Imgraph-core project to test core functionalities of Steffi, these tests are executed on an Steffi datbases
running on a single machine. To execute the tests, on the Steffi-core folder execute the following command:

```sh
	mvn compile test
```
