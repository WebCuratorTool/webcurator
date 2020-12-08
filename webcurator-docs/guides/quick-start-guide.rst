==========================
Quick Start Guide
==========================

Introduction
=====================

About the Web Curator Tool
--------------------------

The Web Curator Tool is a tool for managing the selective web harvesting
process. It is typically used at national libraries and other collecting
institutions to preserve online documentary heritage.

Unlike previous tools, it is enterprise-class software, and is designed
for non-technical users like librarians. The software is developed
jointly by the National Library of New Zealand and the National Library of
the Netherlands, and released as open source software for the benefit of the
international collecting community.

About this document
-------------------

This document describes how to set up the Web Curator Tool on a Linux system
in the simplest possible way. Its intended audience are users who want to quickly 
install and try out the software.

For a proper production set up, see the :doc:`System Administrator Guide <system-administrator-guide>`

Installation
=========================

Prerequisites
-------------

* Java 8 or higher
* MySQL 5.0.95 or newer. PostgreSQL and Oracle are also supported, but in this Quick Start Guide we'll be using MySQL/MariaDB
* Heritrix 3.3.0 or newer


Setting up Heritrix 3
---------------------

We're assuming that Java and MySQL have already been set up. For Heritrix 3.3.0, we'll be using a recent
stable build of the 3.3.0 branch. The Heritrix 3 Github wiki contains a section detailing the current master
builds available https://github.com/internetarchive/heritrix3/wiki#master-builds.

Unzip the archive containing the Heritrix binary, go into the resulting directory and execute the following:

::

	user@host:/usr/local/heritrix-3.3.0-SNAPSHOT$ cd bin
	user@host:/usr/local/heritrix-3.3.0-SNAPSHOT/bin$ 
	user@host:/usr/local/heritrix-3.3.0-SNAPSHOT/bin$ ./heritrix -a admin

This starts up Heritrix with the password "admin" for the user admin, which is the default set of credentials
used by the WCT Harvest Agent. You can also specify the Heritrix jobs directory using the ``-j`` parameter.
Otherwise the default will be used **<HERITRIX_HOME>/jobs**.


Creating the database
---------------------

Download the latest stable binary WCT release from https://github.com/WebCuratorTool/webcurator/releases/.
Extract the archive and go into the resulting directory (in our case **/tmp/wct**). Then, to create the
WCT database and its objects, run the script set-up-mysql.sh (found in the db subdirectory):

::

	user@host:/tmp/wct$ cd db
	user@host:/tmp/wct/db$ ./set-up-mysql.sh

*You'll need to set the variable* ``$MYSQL_PWD`` *in this script to the correct value for your MySQL
installation.*


Deploying and configuring the WCT components
--------------------------------------------

To deploy the WCT components, copy the files inside the **/tmp/wct/lib/** folder to an appropriate directory for
running the application.

::

   user@host:/tmp/wct$ cd lib
   user@host:/tmp/wct/lib$ cp * /usr/local/wct

   /usr/local/wct/webcurator-webapp-3.0.0.war
   /usr/local/wct/webcurator-store-3.0.0.war
   /usr/local/wct/webcurator-harvest-agent-h3-3.0.0.jar
   /usr/local/wct/webcurator-harvest-agent-h1-3.0.0.jar


By default WCT assumes the existence of a directory **/usr/local/wct**, where it stores all
its files. If you want to follow this default, make sure that this directory exists and is
writable for the user that will run the application.

To use an alternative location, create a file **application.properties** inside the directory 
where you've copied the war files, with the following content:

::


   arcDigitalAssetStoreService.baseDir=/tmp/wct-files/store
   harvestAgent.baseHarvestDirectory=/tmp/wct-files/harvest-agent


where the parent directory (in this case **/tmp/wct-files**) must exist and be writable for the application.
The value of **arcDigitalAssetStoreService.baseDir** is the directory where the store component will store
the harvest data (logs, warc files) and **harvestAgent.baseHarvestDirectory** is the temporary
storage location for the harvest agent.

*Note, the* ``harvestAgent.baseHarvestDirectory`` *path* **cannot** *match the Heritrix 3 jobs directory. This
will cause a conflict within the H3 Harvest Agent.*

You can now start WCT by running the following commands, after which you should be able to login at
http://localhost:8080/wct, using the user 'bootstrap' and password 'password'.

::

   user@host:/usr/local/wct$ java -jar webcurator-webapp-3.0.0.war
   user@host:/usr/local/wct$ java -jar webcurator-store-3.0.0.war
   user@host:/usr/local/wct$ java -jar webcurator-harvest-agent-h3-3.0.0.jar


*Note, a* ``logs`` *folder will be created automatically in the directory you run the WCT
components in, e.g.* ``/usr/local/wct/logs``.

You can now create users and roles and configure the system. Refer to the User Manual for more information.


Caveats
-------

This document only covers the most simple scenario for setting up WCT and will probably not result in a
system that meets the production requirements of your organisation. Important topics that have not been
covered here:

* WCT can also authenticate users via LDAP (see the :doc:`System Administrator Guide <system-administrator-guide>`)
* By default all communication between the components and between the browser and WCT is unencrypted. To
  enable SSL/TLS, see the :doc:`System Administrator Guide <system-administrator-guide>`
* You can use OpenWayback to view harvests from within WCT, see :doc:`Wayback Integration Guide <wayback-integration-guide>`




