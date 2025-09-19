===================
Upgrade Guide 3.2.4
===================


Introduction
============

This guide, intended for system administrators, covers upgrading to WCT version
3.2.4 from versions 3.1.x or earlier 3.2.x releases.

For information on how to install and setup the Web Curator Tool from scratch,
see the Web Curator Tool System Administrator Guide. For information about
developing and contributing to the Web Curator Tool, see the Developer Guide.
For information on using the Web Curator Tool, see the Web Curator Tool Quick
User Guide and the Web Curator Tool online help.

The source for both code and documentation for the Web Curator Tool can be found
at https://github.com/WebCuratorTool/webcurator/.


Configuration changes
=====================

In release 3.2.4 the options relating to replay URLs in webapp's application.properties have 
been renamed for clarity:

==================================================== ==========================================
Old name                                             New name 
==================================================== ==========================================
harvestResourceUrlMapper.urlMap                      qualityReviewToolController.accessTool.url
qualityReviewToolController.archiveUrl               qualityReviewToolController.archive1.url 
qualityReviewToolController.archiveName              qualityReviewToolController.archive1.name 
qualityReviewToolController.archive.alternative      qualityReviewToolController.archive2.url
qualityReviewToolController.archive.alternative.name qualityReviewToolController.archive2.name
==================================================== ==========================================





