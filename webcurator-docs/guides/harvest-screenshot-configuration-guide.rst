======================================
Harvest Screenshot Configuration Guide
======================================


Introduction
============

The Web Curator Tool is able to generate screenshots of seed URLs for Target Instances. Screenshots are generated from 
the live version and harvested version of each web page that corresponds to a seed URL in the Target Instance.

This guide shows how to deploy and configure an instance of Web Curator Tool to generate harvest screenshots.


Contents of this document
-------------------------

Following this introduction, the Harvest Screenshot Configuration Guide includes the following sections:

-   **Overview** - An overview of the feature.

-   **Java or Python** - Covers the different implementation options.

-   **Installation** - Covers installing necessary 3rd-party tools.

-   **Configuration** - Covers the backend configuration options.

-   **Storage** - Covers where screenshots are stored.


Overview
========

This feature generates two types of screenshots for each Target Instance seed URL. Screenshots of the live URL at the 
time of crawling, and screenshots of the harvested URL when viewed through OpenWayback or Pywb.

For each seed URL there are eight screenshots in total that are generated:

- Browser window sized screenshot of the live URL
- Thumbnail of the browser window sized screenshot of the live URL
- Full page screenshot of the live URL
- Thumbnail of the full page screenshot of the live URL
- Browser window sized screenshot of the harvested URL
- Thumbnail of the browser window sized screenshot of the harvested URL
- Full page screenshot of the harvested URL
- Thumbnail of the full page screenshot of the harvested URL

Thumbnails are used in the WCT UI. Once clicked on, a pop-up window will show the browser window screenshots for each 
seed URL.

Java or Python
==============

There are two implementation options provided for the screenshot generation - built-in Java or Python.

The built-in Java implementation uses the Selenium libraries for Java and requires minimal setup.

If a Python implementation is preferred for using Selenium, then this is also bundled with WCT. This implementation 
requires additional setup of Python and dependencies in your environment.


Installation
============

In order to use the screenshot generation in WCT, minimally Chrome and the Chrome Driver must be installed.

Chrome
------

Install the latest stable release of Google Chrome for your operating system. The recent versions of Chrome now include
'headless' mode, so there is no advantage to specifically installing the headless version of Chrome (except for size).

**Note, the environment where Chrome is installed must have network access to the Internet.**

Installing on RHEL 8 and newer

-   Setup local Chrome yum repo - create /etc/yum.repos.d/google-chrome.repo::
        
        [google-chrome]
        name=google-chrome
        baseurl=https://dl.google.com/linux/chrome/rpm/stable/x86_64
        enabled=1
        gpgcheck=1
        gpgkey=https://dl.google.com/linux/linux_signing_key.pub

-   Check google-chrome-stable package is available

        yum info google-chrome-stable

-   Install google-chrome-stable package

        yum install google-chrome-stable

-   Confirm Chrome version installed 

        google-chrome-stable --version

Installing on Ubuntu

-   Update package manager

        sudo apt update
        
-   Download the google-chrome-stable package

        wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb

-   Install google-chrome-stable package

        sudo apt install ./google-chrome-stable_current_amd64.deb

-   Confirm Chrome version installed 

        google-chrome-stable --version
        

Installing the Chrome driver
----------------------------
The Chrome driver must also be installed to enable interaction with Chrome. The driver must be the same minor version 
as the Chrome version installed.

To locate the correct driver version use https://chromedriver.chromium.org/downloads

Installing on linux environments

-   Download the driver

        wget https://storage.googleapis.com/chrome-for-testing-public/122.0.6261.94/linux64/chromedriver-linux64.zip
        
-   Extract the driver and make executable::

        unzip chromedriver-linux64.zip
        sudo cp chromedriver-linux64/chromedriver /usr/bin/chromedriver
        chmod +x /usr/bin/chromedriver

-   Confirm Chrome driver installed 

        chromedriver -v


Python implementation
---------------------
If using the Python implementation, the following must also be installed:

-   Python 3.x
-   Pip3
-   Selenium Python package

        pip3 install selenium
        
-   Pillow Python package

        pip3 install pillow

-   From the webcurator Github repository, the file `/SeleniumScreenshotCapture/SeleniumScreenshotCapture.py` must be
    copied to the environment where WCT Store is running and specified in your environment's PATH.

*Note, these Python packages and scripts must be accessible from an environment where WCT Store is running.*


Testing Chrome
---------------------

To test headless Chrome in a Linux environment, you can load a URL using the following command or similar

        google-chrome --no-sandbox --headless=new --disable-gpu  <URL>
        

Configuration
===================

Enabling the screenshot generation feature

-   Open your WCT Webapp `application.properties` file and set `enableScreenshots` to true. (`application.properties` is 
    located in `webcurator-webapp.war/WEB-INF/classes/application.properties`)

        enableScreenshots=true

-   Open your WCT Store `application.properties` file and set `enableScreenshots` to true. (`application.properties` is 
    located in `webcurator-store.war/WEB-INF/classes/application.properties`)

        enableScreenshots=true
        
    *Both these settings must be in sync to enable/disable the feature.*
       
-   To stop harvests from crawling if a screenshot of the live website fails, set `abortHarvestOnScreenshotFailure` to 
    true.
        
        abortHarvestOnScreenshotFailure=true

-   Set the Chrome crash pad directory path. Ensure the directory has the necessary permissions so that Chrome can 
    write to it. 

        chromeClashDirectory=/tmp/chrome-crash-screenshot

OpenWayback or Pywb
-------------------

The harvest screenshot generation can work with OpenWayback or Pywb

-   Open your WCT Store `application.properties` file and set the wayback viewer and version used with your WCT 
    installation. `wayback.name` can be set to either *owb* or *pywb*. The available wayback versions tested are 
    OpenWayback 2.4, Pywb 2.6.7, Pywb 2.7.3 and Pywb 2.7.4.::

        wayback.name=pywb
        wayback.version=2.7.3

-   Either enable `waybackIndexer` for OpenWayback or `pywbIndexer` for Pywb. Ensure the indexer not being used is set
    to false.::

        #WaybackIndexer
        # Enable this indexer
        waybackIndexer.enabled=<true or false>
        
        #PYWB integration
        pywbIndexer.enable=<true or false>

Pywb implementation
-------------------

-   Open your WCT Store `application.properties` file, change the `pywbIndexer.wb-manager.store` path to the root directory
    of your Pywb installation, and change the `pywbIndexer.wb-manager.coll` value to the name of the Pywb collection used
    by WCT.::

        pywbIndexer.wb-manager.store=/usr/local/wct/pywb
        pywbIndexer.wb-manager.coll=<my-web-archive>


Python implementation
---------------------

-   Open your WCT Store `application.properties` file, and change screenshotCommand parameters to use the 
    `SeleniumScreenshotCapture.py` Python implementation.::

        screenshotCommand.screen=SeleniumScreenshotCapture.py filepath=%image.png% url=%url% width=1400 height=800
        screenshotCommand.fullpage=SeleniumScreenshotCapture.py filepath=%image.png% url=%url%

        screenshotCommand.windowsize=SeleniumScreenshotCapture.py filepath=%image.png% url=%url% width=%width% height=%height%

To test the Python implementation locally, there is a test Python script located in the webcurator Github repository 
`/SeleniumScreenshotCapture/demo_pywb_api.py` for testing SeleniumScreenshotCapture.py.


Storage
-------

Screenshots generated are stored within a `_snapshots` folder in the WCT Store directory for a Target Instance.