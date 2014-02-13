The repository contains the following files and folders:

- README.txt: This file
- BUILD-INSTRUCTIONS.txt: Detailed instructions to build the Steffi JAR, the Gremlin console and the Rexster server
- USE-INSTRUCTIONS.txt: Instructions to start and stop the data servers, traversal managers, Gremlins console and Rexster server
- steffi-core/ : Folder containing the Maven project of Steffi GraphDB
- neo4J-tests/ : The Maven project used to run the Neo4J experiments described on the thesis
- titan-tests/ : The Maven project used to run the Titan experiments described on the thesis
- msc/ : Example files referenced on the BUILD-INSTRUCTIONS.txt file
- gremlin-scripts/ : Gremlin scripts showing simple examples of the use of Imgraph

You should start by reading the BUILD-INSTRUCTIONS.txt file to generate the executables JARs, then review the USE-INSTRUCTIONS.txt file to know
how to control the Imgraph's main components and the Gremlin and Rexster interfaces. 


There is a set of unit tests in the imgraph-core project to test core functionalities of Steffi, these tests are executed on a Steffi database
running on a single machine. To execute the tests, on the imgraph-core folder execute the following command:

	mvn compile test
