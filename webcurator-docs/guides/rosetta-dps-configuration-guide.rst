===============================
Rosetta DPS Configuration Guide
===============================


Introduction
============

The Web Curator Tool is able to archive harvests to the Rosetta Digital Preservation System (DPS). The National Library
of New Zealand currently uses Rosetta DPS for archiving their harvests from WCT.

This guide shows how to deploy and configure an instance of Web Curator Tool to work with Rosetta DPS.


Contents of this document
-------------------------

Following this introduction, the Rosetta DPS Configuration Guide includes the following sections:

-   **Configuration steps** - Covers the backend configuration options.

-   **User Interface adjustment** - Covers adjusting fields/values in the UI.

-   **More information** - Provides some links for more information.

*All configuration for this integration is inside `application.properties`. (This file is located in `webcurator-store.war/WEB-INF/classes/`.*


Configuration steps
===================

Enable Rosetta DPS archiving
----------------------------
::

    # The archive type to use for this installation (one of: fileArchive, omsArchive, dpsArchive).
    arcDigitalAssetStoreService.archive=dpsArchive

Configure the Rosetta Server
----------------------------
::

    dpsArchive.pdsUrl=http://xxxserverxxx.xxx.xxx.xx/pds
    dpsArchive.ftpHost=xxxftpserverxxx.xxx.xxx.xx
    dpsArchive.ftpUserName=<ftp_username>
    dpsArchive.ftpPassword=<ftp_password>
    dpsArchive.dpsUserInstitution=INS00
    dpsArchive.dpsUserName=<rosetta_username>
    dpsArchive.dpsUserPassword=<rosetta_password>
    dpsArchive.materialFlowId=<rosetta_material_flow_ID>
    dpsArchive.producerId=<rosetta_producer_ID>
    dpsArchive.depositServerBaseUrl=http://xxxserverxxx.xxx.xxx.xx
    dpsArchive.producerWsdlRelativePath=/dpsws/deposit/ProducerWebServices?wsdl
    dpsArchive.depositWsdlRelativePath=/dpsws/deposit/DepositWebServices?wsdl

Set your access restriction codes
---------------------------------
::

    #OMS Codes (Rosetta)
    dpsArchive.dnx_open_access=1020
    dpsArchive.dnx_published_restricted=1021
    dpsArchive.dnx_unpublished_restricted_location=1022
    dpsArchive.dnx_unpublished_restricted_person=1023

Custom deposit form configuration
---------------------------------

DPSArchive uses the following two parameters to determine whether a custom deposit form needs to be displayed before
submitting an HTML Serial harvest. Configure the following parameters to reflect:

-   The name of the agency that would normally harvest/ingest HTML serials
-   The Dublin Core *Type* that would represent the target for an HTML serial

*If there are more than one value for each of these, separate them using comma.*

::

    dpsArchive.htmlSerials.agencyNames=Electronic Serials Harvesting
    dpsArchive.htmlSerials.targetDCTypes=eSerial,eMonograph

URLs that WCT Core would use to display the custom deposit form for each of the target types, separated by comma.
A note on the format of this URL:

-   If WCT Core and WCT Digital Asset Store are deployed in the same Tomcat instance, use a relative URL.
-   If they are deployed in different machines or Tomcat instances, use absolute URL based on WCT DAS' host/port.
    ::

        dpsArchive.htmlSerials.customDepositFormURLs=/wct-store/customDepositForms/rosetta_custom_deposit_form.jsp

-   The material flow ID for each of the target types, separated by comma. There should be one entry for each target
    type defined above.
    ::

        dpsArchive.htmlSerials.materialFlowIds=52063,52073

-   The IE Entity Type for each of the target types, separated by comma. There should be one entry for each target type
    defined above.
    ::

        dpsArchive.htmlSerials.ieEntityTypes=HTMLSerialIE,HTMLMonographIE


User Interface adjustment
=========================

In the event that multiple targetDCTypes are added (as per above), then they need to be made available through the user
interface.

|screenshot_TargetType|

-  Configuration for this list of types must be modified within the Webapp source code, located inside
   `ListsConfig.java`. (This file is located in `webcurator-webapp/src/main/java/org/webcurator/webapp/beans/config/`).
   The Webapp must then be re-compiled from source.

-   The value should match the `targetDCType` set in `webcurator-store.war/WEB-INF/classes/application.properties`.
    ::

        @Bean
        @Scope(BeanDefinition.SCOPE_SINGLETON)
        @Lazy(false)
        public WCTTreeSet dublinCoreTypesList() {
            List<String> initialList = new ArrayList<>();
            initialList.add("");
            initialList.add("Collection");
            initialList.add("Dataset");
            initialList.add("Event");
            initialList.add("Image");
            initialList.add("Interactive Resource");
            initialList.add("Moving Image");
            initialList.add("Physical Object");
            initialList.add("Service");
            initialList.add("Software");
            initialList.add("Sound");
            initialList.add("Still Image");
            initialList.add("Text");
            initialList.add("eSerial");

            WCTTreeSet bean = new WCTTreeSet(initialList, 50);

            return bean;
        }

More information
================

The following guides can provide additional information:

-   :doc:`System Administrator Guide <system-administrator-guide>`

-   :doc:`Developer Guide <developer-guide>`

-   :doc:`Troubleshooting Guide <troubleshooting>`

-   :doc:`FAQ <faq>`


.. |screenshot_TargetType| image:: ../_static/rosetta-dps-configuration-guide/screenshot_TargetType.jpg
   :width: 785.0px
   :height: 298.0px
   :alt: User interface types
