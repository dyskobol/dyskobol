DYSKOBOL
++++++++



.. Contents::


INTRODUCTION
============


Dyskobol is a system, which provides a way to effective metadata extracting of digital content from various drives.


PREREQUISITES
=============
Dyskobol requires the following tools/software installed for your platform:

JAVA TOOLS
----------

1) `Java SE Development Kit 8`__

.. code-block:: bash

    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java8-installer

__ http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html


2) `Apache Ant`__

.. code-block:: bash

    sudo apt-get install ant


__ https://ant.apache.org/



SCALA TOOLS
-----------

1) `Scala & SBT`__

.. code-block:: bash

    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
	sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
	sudo apt-get update
	sudo apt-get install sbt



__ https://www.scala-lang.org/


C TOOLS
-------

1) build-essential
    .. code-block:: bash

	    sudo apt-get install build-essential

2) autotools-dev
    .. code-block:: bash

	    sudo apt-get install autotools-dev
3) autoconf
    .. code-block:: bash

	    sudo apt-get install autoconf
4) automake
    .. code-block:: bash

	    sudo apt-get install automake
5) libtool
    .. code-block:: bash

	    sudo apt-get install libtool
6) cmake
    .. code-block:: bash

	    sudo apt-get install cmake

LIBRARIES
---------

1) afflib
    .. code-block:: bash

	    sudo apt-get install libafflib-dev
2) libewf
    .. code-block:: bash

	    sudo apt-get install libewf-dev
3) `The Sleuthkit v4.6.0`__

   .. code-block:: bash

        wget https://github.com/sleuthkit/sleuthkit/releases/download/sleuthkit-4.6.0/sleuthkit-4.6.0.tar.gz
        unzip sleuthkit-4.6.0.tar.gz
        cd sleuthkit-4.6.0.tar.gz
        sudo chmod u+x ./bootstrap
        sudo ./bootstrap
        sudo ./configure
        sudo make && sudo make install

__ http://sleuthkit.org/


INSTALLING DYSKOBOL
===================


Development version from Git
----------------------------
1) Downloading repository

Use the command::

    >>> git clone https://github.com/dyskobol/dyskobol.git

2) Building

Execute the command in root directory of Dyskobol project::

    >>> sbt packageBin

USING DYSKOBOL
==============

CONFIGURATION
-------------

First, you have to make a configuration file dyskobol.conf with content:

   .. code-block:: scala

        dyskobol {
                dbs{
                        postgres = [
                        {
                        	host = <host address>
                         	dbName = <database name>
                         	username = <user_name>
                         	password = <password>

                        }
                        ]

                }
                imagePath = <image path>

        }
where

<host address>
  Addres of database server.

  examples
	- localhost
	- 192.168.1.163:5432

<database name>
  Name of database.

<image path>
    Path to disk image which, should be processed.


Example configuration:

.. code-block:: scala

	dyskobol {
	  dbs{
		postgres = [
		  {
			host = localhost
			dbName = postgres
			username = postgres
			password = postgres
		  }
		]

	  }
	  imagePath = ./core/res/test.iso

	}



RUNNING
-------
To run Dyskobol, please execute the command:

.. code-block:: bash

    sbt "core/run path_to_configuration_file"




