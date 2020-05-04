WebCurator Legacy Lib Dependencies
==================================

Legacy jar and other libraries used for building, installing and running WebCurator components.

Synopsis
--------

The webcurator projects require various jars and other libraries to build and run the different components, such as the
`webcurator-webapp`_. This repository contains all these third party artifacts. Ideally these artifacts would be
available through public artifact repositories like the `Central Maven Repository`_, but currently the codebase depends
on many that are not. The process of upgrading the WebCurator components will eventually remove these legacy
dependencies. WebCurator Legacy Lib Dependencies is part of the `WebCuratorTool set of applications`_.


Build and installation
----------------------

Complete build and installation instructions and how `webcurator-legacy-lib-dependencies` fits into that build and
installation process can be found in the `WebCurator documentation repository`_ in the *Developer Guide*.


Issues
------

Issues are tracked through the git repository issue tracker.


License
-------

|copy| 2006 |---| 2018 The National Library of New Zealand, Koninklijke Bibliotheek and others. See individual
commits to determine code authorship. Apache 2.0 License. Third party libraries are copyright their respective producers.

.. _`Central Maven Repository`: https://repo.maven.apache.org/maven2/
.. _`WebCuratorTool set of applications`: https://github.com/WebCuratorTool
.. _`webcurator-webapp`: https://github.com/WebCuratorTool/webcurator-webapp
.. _`WebCurator documentation repository`: https://github.com/WebCuratorTool/webcurator-docs
.. |copy| unicode:: 0xA9 .. copyright sign
.. |---| unicode:: 0x2014 .. m-dash
