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


FindFile is a small Swing application that demonstrates use of Persistit's
KeyFilter class, the Exchange pool, and use of Persistit with Swing.

To use FindFile, first build a text file containing a (large) list of file
names. To to this, use one of these commands:

	Windows:	dir /b /s C:\ >> FileNames.txt
	Unix/Linux:	ls -1 -p -R > FileNames.txt

After launching FindFile, specify this file as the source file to load and 
click the "Load" button.

Or you can specify the full path name of a directory, press the "Load" button
and crawl the file system directory.

This demo loads the file names from a text file rather than 
enumerating all the files on the disk through Java file operations
for simplicity, and also because we want you to see that Persistit 
is capable of loading and indexing a lot of information in just a few 
seconds.

To build FindFile:

	Run Ant on build.xml in this directory (target "compile")
	
	- or -
	
	javac -classpath ../../core/target/akiban-persistit-xx.yy.zz-SNAPSHOT-jar-with-dependencies-and-tests.jar FindFile.java

To run FindFile:

	Run Ant on build.xml in this directory (target "run")
	
	- or -
	
	java -classpath ../../core/target/akiban-persistit-xx.yy.zz-SNAPSHOT-jar-with-dependencies-and-tests.jar;. FindFile

Persistit will place a volume file in paths specified by persistit.properties.
You can change the location of these files by modifying the datapath property 
of this configuration file.
