WebCurator DB
=============

WebCurator database creation and upgrade scripts as well as database repository code.

Synopsis
--------

The WebCurator requires a database. This project contains the database creation and upgrade scripts. WebCurator DB
is part of the `WebCuratorTool set of applications`_.


Building and installation
-------------------------

Complete build and installation instructions can be found in the `WebCurator documentation repository`_ in the
*Developer Guide*.

Artifacts
~~~~~~~~~
There are three artifacts produced by a build:

-   `webcurator-db-latest-<version>.zip` - These are the latest database creation and migration scripts.

-   `webucrator-db-legacy-<version>.zip` - These are the previous versions of the database creation and migration scripts.

-   `webcurator-db-repository-<version>.jar` - This is a jar that contains the java code for interacting with the
    database. It is required by the *webcurator-webapp* application.

Build commands
~~~~~~~~~~~~~~
Gradle is used to build the project. Build commands are as follows.

Build within the project::

    gradle clean build

Build and publish to the local maven repository::

    gradle clean build publishToMavenLocal

Including the webcurator-db-repository artifact in a project
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
In a project that uses the `webcurator-db-repository` artifact, include the following:

For a gradle project in the dependencies block of the project (version `3.0.0` is used in the example)::

    implementation 'org.webcurator:webcurator-db-repository:<version>'

For a maven project in the dependencies section of the project (version `3.0.0` is used in the example)::

        <dependency>
            <groupId>org.webcurator</groupId>
            <artifactId>webcurator-db-repository</artifactId>
            <version>3.0.0</version>
            <scope>compile</scope>
            </exclusions>
        </dependency>


Contributors
------------

See individual git commits for code authorship. Issues are tracked through the git repository issue tracker.

License
-------

|copy| 2006 |---| 2019 The National Library of New Zealand, Koninklijke Bibliotheek and others. See individual
commits to determine code authorship. Apache 2.0 License.

.. _`WebCuratorTool set of applications`: https://github.com/WebCuratorTool
.. _`WebCurator documentation repository`: https://github.com/WebCuratorTool/webcurator-docs
.. |copy| unicode:: 0xA9 .. copyright sign
.. |---| unicode:: 0x2014 .. m-dash
