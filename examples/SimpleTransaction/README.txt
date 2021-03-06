====
    Copyright © 2011-2012 Akiban Technologies, Inc.  All rights reserved.
    
    This program and the accompanying materials are made available
    under the terms of the Eclipse Public License v1.0 which
    accompanies this distribution, and is available at
    http://www.eclipse.org/legal/epl-v10.html
    
    This program may also be available under different license terms.
    For more information, see www.akiban.com or contact licensing@akiban.com.
    
    Contributors:
    Akiban Technologies, Inc.
====


SimpleTransaction performs the following steps:

-  Initializes com.persistit.Persistit using a Properties file.
-  Launches multiple threads that concurrently perform simple transactions. 
-  Each transaction randomly transfers "money" between "accounts"
   such that the sum of all "accounts" remains unchanged.
-  Closes Persistit to ensure all records have been written to disk.

To build SimpleTransaction:

	Run Ant on build.xml in this directory (target "compile")
	
	- or -
	
	javac -classpath ../../target/akiban-persistit-2.1-SNAPSHOT-jar-with-dependencies-and-tests.jar SimpleTransaction.java

To run SimpleTransaction:

	Run Ant on build.xml in this directory (target "run")
	
	- or -
	
	java -classpath ../../target/akiban-persistit-2.1-SNAPSHOT-jar-with-dependencies-and-tests.jar;. SimpleTransaction

Persistit will place a volume file in paths specified by persistit.properties.
You can change the location of these files by modifying the datapath property
of this configuration file.

To verify that transactional integrity is preserved across failures:

	Interrupt SimpleTransaction as it runs by stopping the JVM, shutting down
	the operating system or powering off the computer.
	Restart SimpleTransaction to get a recalculated "balance".  The 
	result should be zero.
	
