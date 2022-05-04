=================
Upgrade Guide 3.1
=================


Introduction
============

This guide, intended for system administrators, covers upgrading to WCT version
3.1 from versions 3.0.x.

For information on how to install and setup the Web Curator Tool from scratch,
see the Web Curator Tool System Administrator Guide. For information about
developing and contributing to the Web Curator Tool, see the Developer Guide.
For information on using the Web Curator Tool, see the Web Curator Tool Quick
User Guide and the Web Curator Tool online help.

The source for both code and documentation for the Web Curator Tool can be found
at https://github.com/WebCuratorTool/webcurator/.

Contents of this document
-------------------------

Following this introduction, the Web Curator Tool Upgrade Guide includes the
following sections:

-   **Upgrade requirements** - Covers requirements for upgrading.

-   **Shut Down the WCT** - Describes shutting down WCT prior to upgrading.

-   **Upgrading the WCT database schema** - Describes how to upgrade the
    database schema.

-   **Upgrading the application** - How to upgrade the application.

-   **Configuration** - New configuration parameters.

-   **Post-upgrade notes** - Additional post migration steps.

Upgrade requirements
====================

The following section explains the requirements for upgrading to version 3.1
of the Web Curator Tool.

Prerequisites
-------------

The following are required to successfully upgrade the WCT to version 3.1:  

-   Installed and running version of the Web Curator Tool - version 3.0.x
    running against Oracle `11g` or newer, PostgreSQL `8.4.9` or newer, or
    MySQL `5.0.95` or newer. 

-   Access to the server(s) for the Webapp, Digital Asset Store, and Harvest
    Agent components. 

*Note that the Web Curator Tool has been tested with Oracle `11g`, PostgreSQL
`8.4.9` and `9.6.11`, MySQL `5.0.95` and MariaDB `10.0.36`, although newer
versions of these products are expected to work as well. Due to the use of
Hibernate for database persistence other database platforms should work, if the
product is rebuilt with the correct database dialect, using the required JDBC
driver. However, only MySQL, PostgreSQL and Oracle have been tested.*

Infrastructure Considerations
-----------------------------

WCT version 3.1 introduces significant changes around how the application is run
and managed.

Backup Database
~~~~~~~~~~~~~~~

Several database tables have been dropped in version 3.1, which hold potentially
hold large amounts of data for some WCT users.

    - ARC_HARVEST_FILE

    - ARC_HARVEST_RESOURCE

    - ARC_HARVEST_RESULT

    - HARVEST_RESOURCE

Whilst the removal of these tables should improve the performance of the database,
it is important to safeguard against any data loss. A database backup prior to this
upgrade is recommended.

Increased Memory for Harvest Visualization
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Increasing the allowed memory for the WCT Webapp and Store components may be
required in loading the Harvest Network Visualization feature for large web
harvests, in the 50GB+ range.

*Particularly relevant to setups where all the WCT components are running on a*
*single server (Webapp, Store, Harvest Agent, Heritrix and OpenWayback or Pywb).*

Retired Heritrix 1 Harvest Agent
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Heritrix 1 Harvest Agent is no longer part of the WCT application and has
been removed.

Shut Down the WCT
=================

The major components to the deployment of the Web Curator Tool are:

-   The WCT Webapp (`webcurator-webapp.war`).

-   The WCT Harvest Agent for Heritrix 3 (`webcurator-harvest-agent-h3.jar`).

-   The WCT Digital Asset Store (`webcurator-store.war`).

To begin the upgrade of the WCT to version 3.1:

1.  Make sure that all target instances have completed.  

2.  Shut down the running instances of the Harvest Agents, Webapp, and
    Digital Asset Store. 


Upgrading WCT Database Schema
=============================

Version 3.1 of the Web Curator Tool is supported under MySQL `5.0.95` and up,
Oracle `11g` and up, and PostgreSQL `8.4.9` and up. Database schema upgrade
scripts have been provided for all three databases.

Upgrade scripts
---------------

Upgrade script names are of the format::

    wct-upgrade-<source-version>-to-<target-version>-<database-type>.sql

where `<database-type>` is one of `mysql`, `oracle` or `postgres`.

The `<source-version>` is the current or source version (the version you're migrating
*from*).

The `<target-version>` is the target version (the version you're migrating *to*).

**No script means no database change.** *If there is no script for a particular
version it means that there were no database changes.*

Upgrades are incremental
------------------------

Upgrade scripts only cover a single upgrade step from one version to another.
This means that upgrading across several versions requires that all the scripts
between the source and target version be executed in sequence.

For example, to upgrade a MySQL database from version 1.4.0 to 3.1, the
following scripts would need to be executed in this order:

From webcurator-db/legacy/upgrade:

#.  `upgrade-mysql-1_6-to-1_6_1.sql`
#.  `wct-upgrade-1_6_1-to-2_0-mysql.sql`
#.  `wct-upgrade-2_0-to-2_0_2-mysql.sql`
#.  `wct-upgrade-2_0_2-to-3_0_0-mysql.sql`

Then, from webcurator-db/latest/upgrade:

#.  `wct-upgrade-3_0-to-3_1-mysql.sql`

*Note that some scripts may complain about columns already existing or timestamp column definitions having*
*the wrong precision. You can safely ignore these errors. You might also get warnings about implicit indexes*
*being created. These are harmless as well.*


Upgrading from 3.0.x to 3.1
---------------------------

Run the following upgrade scripts:

-  From **webcurator-db/latest/upgrade** ::

    1. wct-upgrade-3_0-to-3_1-<database-type>.sql

Upgrading on Oracle
-------------------

This guide assumes that the source version's schema is already configured on
your Oracle database under the schema `DB_WCT`.

1.  Log on to the database using the `DB_WCT` user.

2.  Run the following SQL to upgrade the database::

        db[/legacy]/upgrade/wct-upgrade-<source-version>-to-<target-version>-oracle.sql

        SQL> conn db_wct@<sid-name>

        SQL> @wct-upgrade-<source-version>-to-<target-version>-oracle.sql

        SQL> exit;


Upgrading on PostgreSQL
-----------------------

This guide assumes that the source version's schema is already configured on
your PostgreSQL database under the schema `DB_WCT`.

1.  Log on to the database using the `postgres` user.

2.  Run the following SQL to upgrade the database::

        db[/legacy]/upgrade/wct-upgrade-<source-version>-to-<target-version>-postgres.sql

        postgres=# \c Dwct

        postgres=# \i wct-upgrade-<source-version>-to-<target-version>-postgres.sql

        postgres=# \q

Upgrading on MySQL
------------------

This guide assumes that the previous version's schema is already configured on
your MySQL database under the schema `DB_WCT`.

1.  Log on to the database using the `root` user.

2.  Run the following SQL to upgrade the database::

        db[/legacy]\upgrade\wct-upgrade-<source-version>-to-<target-version>-mysql.sql

        mysql> use db_wct

        mysql> source wct-upgrade-<source-version>-to-<target-version>-mysql.sql

        mysql> quit


Upgrading the application
=========================

Deploying WCT
-------------

3.  Copy any settings/properties/configuration files you wish to keep
    from the previous WCT directories.

4.  Copy the version 3.1 WAR/JAR files into a new dedicated directory for
    running WCT. E.g. ::

        /opt/app/wct/webapp/webcurator-webapp.war
        /opt/app/wct/store/webcurator-store.war
        /opt/app/wct/harvest-agent-h3/webcurator-harvest-agent-h3.jar

5.  Configure the appropriate file and user permissions for running the WCT
    components. Ensure the WCT system user has read and write permission to the
    base directories for WCT Store and the Harvest Agents.

6.  Copy any settings from the old properties and configuration files you backed
    up in step 3. Start from the new configuration files and merge any relevant
    values from your old configuration files back in. ::

        webcurator-webapp.war/WEB-INF/classes/aplication.properties

        webcurator-store.war/WEB-INF/classes/aplication.properties

        harvest-agent-h3.jar/BOOT-INF/classes/aplication.properties


Configuration
=============

See the WCT System Administrator Guide for more information about configuring the Web
Curator Tool.

The Logback XML file (``webcurator-webapp.war/WEB-INF/classes/logback-spring.xml``) should
also be checked as per the WCT System Administrator Guide to ensure their values are
appropriate for your deployment.

New configuration parameters in 3.1
-----------------------------------

**webcurator-webapp.war/WEB-INF/classes/aplication.properties**

The new harvest visualization feature and patching requires a local working directory for Webapp. If this directory doesn't exist, the application will attempt to create it. ::

    # WebApp additional settings
    #####################################
    core.base.dir=/usr/local/wct/webapp/


Post-upgrade notes
==================

Once the Web Curator Tool has been upgraded you will be able to start each WCT
component and log in as any of the users that existed prior to the upgrade.

Notes on the Upgrade Effects
----------------------------

Please see the Release Notes for further information regarding the changes
introduced in WCT 3.1.
