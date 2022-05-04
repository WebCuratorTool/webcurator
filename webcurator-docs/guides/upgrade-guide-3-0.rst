=================
Upgrade Guide 3.0
=================


Introduction
============

This guide, intended for system administrators, covers upgrading to WCT version
3.0 from versions 2.x and 1.6.x. If you are on an earlier version you can still
follow these instructions to upgrade the database, but you will need to manually
merge your old configuration files with the new files, or configure your
installation from scratch.

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

The following section explains the requirements for upgrading to version 3.0
of the Web Curator Tool.

Prerequisites
-------------

The following are required to successfully upgrade the WCT to version 3.0:  

-   Installed and running version of the Web Curator Tool – version 2.x.x (or
    older) running against Oracle `11g` or newer, PostgreSQL `8.4.9` or newer, or
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

WCT version 3.0 introduces significant changes around how the application is run
and managed.

Spring Boot Replaces Tomcat
~~~~~~~~~~~~~~~~~~~~~~~~~~~

As WCT v3.0 now uses Spring Boot, there is no longer a requirement to run WCT
inside a dedicated Apache Tomcat container server. Each WCT component compiles
to a runnable war or jar file, that can be started directly from the command
line using Java. This has some implications:

- Each component (Webapp, Digital Asset Store, and Harvest Agents) must
  now be started individually, and will run as separate Java process.

- If your WCT components run in a distributed server setup (i.e. the Harvest
  Agents run on separate servers to the Webapp and Digital Asset Store), then
  you might need to allow for additional network ports. Previously, WCT could
  run inside Apache Tomcat behind a single network port.

  *Also, if you are using OpenWayback for harvest QA then you will still require*
  *Apache Tomcat, and will need to avoid any port conflicts.*

- With each WCT component running as a separate Java process, closer attention will
  need to be paid to memory usage and allocation. Maximum memory allowance (-Xmx)
  can be passed as a command line parameter when starting any WCT component.

  *Also, if you are using OpenWayback for harvest QA then you will still require*
  *Apache Tomcat, and will need to factor this into your memory allowances.*


Shut Down the WCT
=================

The major components to the deployment of the Web Curator Tool are:

-   The WCT Webapp (`webcurator-webapp.war`).

-   The WCT Harvest Agent for Heritrix 1 (`webcurator-harvest-agent-h1.jar`,
    optional, only needed if Heritrix 1 support is desired).

-   The WCT Harvest Agent for Heritrix 3 (`webcurator-harvest-agent-h3.jar`).

-   The WCT Digital Asset Store (`webcurator-store.war`).

        **Upgrading from 1.6.x and earlier**
            *Note that the `wct-agent.war` module has been replaced by two*
            *new modules* ``webcurator-harvest-agent-h1.jar`` *and* ``webcurator-harvest-agent-h3.jar``.

To begin the upgrade of the WCT to version 3.0:

1.  Make sure that all target instances have completed.  

2.  Shut down the Tomcat instance(s) running the Harvest Agents, WCT Core, and
    Digital Asset Store. 


Upgrading WCT Database Schema
=============================

Version 3.0 of the Web Curator Tool is supported under MySQL `5.0.95` and up,
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

For example, to upgrade a MySQL database from version 1.4.0 to 3.0, the
following scripts would need to be executed in this order:

From webcurator-db/legacy/upgrade:

#.  `upgrade-mysql-1_4-to-1_4_1.sql`
#.  `upgrade-mysql-1_5-to-1_5_1.sql`
#.  `upgrade-mysql-1_5_1-to-1_5_2.sql`
#.  `upgrade-mysql-1_5_2-to-1_6.sql`
#.  `upgrade-mysql-1_6-to-1_6_1.sql`
#.  `wct-upgrade-1_6_1-to-2_0-mysql.sql`
#.  `wct-upgrade-2_0-to-2_0_2-mysql.sql`

Then, from webcurator-db/latest/upgrade:

#.  `wct-upgrade-2_0_2-to-3_0_0-mysql`

*Note that some scripts may complain about columns already existing or timestamp column definitions having*
*the wrong precision. You can safely ignore these errors. You might also get warnings about implicit indexes*
*being created. These are harmless as well.*


Upgrading from 2.0.2 to 3.0
---------------------------

Run the following upgrade scripts:

-  From **webcurator-db/latest/upgrade** ::

    1. wct-upgrade-2_0_2-to-3_0_0-<database-type>.sql

Upgrading from 2.0 to 3.0
-------------------------

Run the following upgrade scripts:

-  From **webcurator-db/latest/upgrade** ::

    1. wct-upgrade-2_0-to-2_0_2-<database-type>.sql
    2. wct-upgrade-2_0_2-to-3_0_0-<database-type>.sql

wct-upgrade-<source-version>-to-<target-version>-<database-type>.sql

Upgrading from 1.6.2 to 3.0
---------------------------

Run the following upgrade scripts:

-  From **webcurator-db/latest/upgrade** ::

    1. wct-upgrade-1_6_1-to-2_0-<database-type>.sql
    2. wct-upgrade-2_0-to-2_0_2-<database-type>.sql
    3. wct-upgrade-2_0_2-to-3_0_0-<database-type>.sql

Upgrading from pre-1.6.x to 3.0
-------------------------------

Run the following upgrade scripts:

- From **webcurator-db/legacy/upgrade**, all scripts onwards from your current WCT
  version, in ascending order, taking the database version up to 1.6.1.

-  From **webcurator-db/latest/upgrade** ::

    1. wct-upgrade-1_6_1-to-2_0-<database-type>.sql
    2. wct-upgrade-2_0-to-2_0_2-<database-type>.sql
    3. wct-upgrade-2_0_2-to-3_0_0-<database-type>.sql

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
    from the old Apache Tomcat webapps directory.

4.  Remove the applications from the Apache Tomcat webapps directory, including
    the expanded directory and WAR files.

5.  Copy the version 3.0 WAR/JAR files into a new dedicated directory for
    running WCT. E.g. ::

        /opt/app/wct/webapp/webcurator-webapp.war
        /opt/app/wct/store/webcurator-store.war
        /opt/app/wct/harvest-agent-h3/webcurator-harvest-agent-h3.jar

6.  Configure the appropriate file and user permissions for running the WCT
    components. Ensure the WCT system user has read and write permission to the
    base directories for WCT Store and the Harvest Agents.

7.  Copy any settings from the old properties and configuration files you backed
    up in step 3. Start from the new configuration files and merge any relevant
    values from your old configuration files back in. ::

        wct/WEB-INF/classes/wct-core.properties -> webcurator-webapp.war/WEB-INF/classes/aplication.properties
        wct/META-INF/context.xml -> webcurator-webapp.war/WEB-INF/classes/aplication.properties
        wct/styles/styles.css -> webcurator-webapp.war/styles/styles.css

        wct-store/WEB-INF/classes/wct-das.properties -> webcurator-store.war/WEB-INF/classes/aplication.properties

        harvest-agent-h3/WEB-INF/classes/wct-agent.properties -> harvest-agent-h3.jar/BOOT-INF/classes/aplication.properties

        harvest-agent-h1/WEB-INF/classes/wct-agent.properties -> harvest-agent-h1.jar/BOOT-INF/classes/aplication.properties


Configuration
=============

See the WCT System Administrator Guide for more information about configuring the Web
Curator Tool.

The Logback XML file (``webcurator-webapp.war/WEB-INF/classes/logback-spring.xml``) should
also be checked as per the WCT System Administrator Guide to ensure their values are
appropriate for your deployment.

New configuration parameters in 3.0
-----------------------------------

**webcurator-webapp.war/WEB-INF/classes/aplication.properties**

Configuration option for specifying a local Spring profile which can supplement or override
the default application.properties file. ::

    spring.profiles.active=local+mysql

The application context is now configurable through the WCT properties. Previously this was
managed by Apache Tomcat and configurable by renaming the ``wct.war`` file. ::

    server.servlet.contextPath=/wct

Logback logging path, relative to the directory the WCT component is running from. ::

    logging.path=logs/


**webcurator-store.war/WEB-INF/classes/aplication.properties**

Configuration option for specifying a local Spring profile which can supplement or override
the default application.properties file. ::

    spring.profiles.active=local

Logback logging path, relative to the directory the WCT component is running from. ::

    logging.path=logs/


**harvest-agent-h3.jar/BOOT-INF/classes/aplication.properties**

Configuration option for specifying a local Spring profile which can supplement or override
the default application.properties file. ::

    spring.profiles.active=local

The file transfer mode between the Harvest Agents and WCT Store can be toggled between a local
file copy when a single server is used, and streaming of files when the Harvest Agent and Store
are located on separate servers. ::

    # the file transfer mode from harvest agent to store component:
    # 1) copy: when Harvest Agent and Store Component are deployed on the same machine;
    # 2) stream: when Harvest Agent and Store Component are distributed deployed on different machines;
    digitalAssetStore.fileUploadMode=copy


New configuration parameters in 2.0
-----------------------------------

**webcurator-webapp.war/WEB-INF/classes/aplication.properties**

There's a new variable that tells the core where to find its Heritrix 3 scripts
(used by the H3 script console).
::

    h3.scriptsDirectory=/usr/local/wct/h3scripts


**harvest-agent-h3.jar/BOOT-INF/classes/aplication.properties**

The harvest agent now needs to have a (unique) name and the path of its logReaderService must
be specified. (This variable is also needed in the wct-agent.properties file for
Heritrix 1 agents.)
::

    harvestAgent.service=My Agent
    harvestAgent.logReaderService=/harvest-agent-h3/services/urn:LogReader

There are now settings that tell the agent how to connect to its Heritrix 3 instance.
::

    h3Wrapper.host=localhost
    h3Wrapper.port=8443
    h3Wrapper.keyStoreFile=
    h3Wrapper.keyStorePassword=
    h3Wrapper.userName=admin
    h3Wrapper.password=admin


New configuration parameters in 1.6.3
-------------------------------------

**webcurator-store.war/WEB-INF/classes/aplication.properties**

Changes required by the National Library of New Zealand to be compatible with archiving
to a Rosetta DPS integrated with Alma (library cataloguing and workflow management system
from Ex Libris). All changes have been implemented as backward compatible as possible. The
exposure of these changes and their configuration are through the files wct-das.properties,
wct-das.xml inside WCT-Store.

Setting Mets CMS section
~~~~~~~~~~~~~~~~~~~~~~~~

The section used in the DNX TechMD for the CMS data is now configurable. The CMS section
can be set to either of the following inside wct-das.properties
::

    dpsArchive.cmsSection=CMS
    dpsArchive.cmsSystem=ilsdb

    OR

    dpsArchive.cmsSection=objectIdentifier
    dpsArchive.cmsSystem=ALMA

Preset producer ID for custom deposit forms
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The Producer ID can now be preset for deposits that use a custom form, particularly useful
if only one Producer is used and saves the user having to input their Rosetta password
each time to search for one. If no Producer ID is set in wct-das.properties then it will
revert to the old process of loading a list of available Producers from Rosetta.
::

    dpsArchive.htmlSerials.producerIds=11111

Toggle HTML Serial agencies using non HTML Serial entity types
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Used when a user is under an HTML Serial agency but wants to submit a custom type. Set to *False*
to enable the use of custom types.
::

    dpsArchive.htmlSerials.restrictAgencyType=true

Custom Types
~~~~~~~~~~~~

Custom Types for Web Harvests, follow the same method as the htmlSerials. If there are more
than one value for each of these, separate them using comma. Make sure there is an equal
number of values for each attribute.
::

    dpsArchive.webHarvest.customTargetDCTypes=eMonograph
    dpsArchive.webHarvest.customerMaterialFlowIds=11111
    dpsArchive.webHarvest.customerProducerIds=11111
    dpsArchive.webHarvest.customIeEntityTypes=HTMLMonoIE
    dpsArchive.webHarvest.customDCTitleSource=TargetName

Set source of Mets DC Title for custom types
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For custom entity tpes, the field of which the Mets DC Title gets populated with for
the mets.xml can now be set. The available fields are the Target Seed Url or the Target
Name. This is switched in wct-das.properties.
::

    dpsArchive.webHarvest.customDCTitleSource=SeedUrl

    OR

    dpsArchive.webHarvest.customDCTitleSource=TargetName


New configuration parameters in 1.6.2
-------------------------------------

**webcurator-store.war/WEB-INF/classes/aplication.properties**

There is now the option of setting Rosetta access codes for when archiving
harvests to the Rosetta DPS.
::

    dpsArchive.dnx_open_access=XXX
    dpsArchive.dnx_published_restricted=XXX
    dpsArchive.dnx_unpublished_restricted_location=XXX
    dpsArchive.dnx_unpublished_restricted_person=XXX

These will only be used if the archive type is set to ‘dpsArchive’.
::

    arcDigitalAssetStoreService.archive=dpsArchive


Updating older configurations
-----------------------------

To update the configuration files when migrating from versions older than
1.6.2, it is recommended to start from the new configuration files and merge
any relevant differences with your existing configuration back in as needed. In
most cases new variables have been added. Only rarely have variables been
dropped or renamed.



Post-upgrade notes
==================

Once the Web Curator Tool has been upgraded you will be able to start each WCT
component and log in as any of the users that existed prior to the upgrade.

Notes on the Upgrade Effects
----------------------------

Please see the Release Notes for further information regarding the changes
introduced in WCT 3.0.
