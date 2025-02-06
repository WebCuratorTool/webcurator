| **privilege**
| A privilege can have one of the following values:

================================ ================== ===================================================
**Privilege**                    **Scope options**  **Grouping**
================================ ================== ===================================================
LOGIN                            NONE               Authentication
MODIFY_OWN_CREDENTIALS	         OWNER              Authentication
CREATE_SITE	                     AGENCY             Manage Copying Permissions and Access Rights
MODIFY_SITE	                     ALL, AGENCY        Manage Copying Permissions and Access Rights
CONFIRM_PERMISSION	             ALL, AGENCY        Manage Copying Permissions and Access Rights 
MODIFY_PERMISSION	             ALL, AGENCY        Manage Copying Permissions and Access Rights
TRANSFER_LINKED_TARGETS	         ALL, AGENCY        Manage Copying Permissions and Access Rights
ENABLE_DISABLE_SITE	             ALL, AGENCY        Manage Copying Permissions and Access Rights
GENERATE_TEMPLATE	             AGENCY             Manage Copying Permissions and Access Rights
CREATE_TARGET	                 AGENCY             Manage Targets
MODIFY_TARGET                    ALL, AGENCY, OWNER Manage Targets
APPROVE_TARGET	                 ALL, AGENCY, OWNER Manage Targets
CANCEL_TARGET                    ALL, AGENCY, OWNER Manage Targets
DELETE_TARGET                    ALL, AGENCY, OWNER Manage Targets
REINSTATE_TARGET                 ALL, AGENCY, OWNER Manage Targets
ADD_SCHEDULE_TO_TARGET           ALL, AGENCY, OWNER Manage Targets
SET_HARVEST_PROFILE_LV1          AGENCY             Manage Targets
SET_HARVEST_PROFILE_LV2          AGENCY             Manage Targets
SET_HARVEST_PROFILE_LV3          AGENCY             Manage Targets
MANAGE_TARGET_INSTANCES          ALL, AGENCY, OWNER Manage Harvests
LAUNCH_TARGET_INSTANCE_IMMEDIATE ALL, AGENCY        Manage Harvests
MANAGE_WEB_HARVESTER	         ALL                Manage Harvests
ENDORSE_HARVEST                  ALL, AGENCY, OWNER Manage Harvests
ARCHIVE_HARVEST                  ALL, AGENCY, OWNER Manage Harvests
UNENDORSE_HARVEST                ALL, AGENCY, OWNER Manage Harvests
CREATE_GROUP                     ALL, AGENCY        Manage Target Groups
MANAGE_GROUP                     ALL, AGENCY, OWNER Manage Target Groups
ADD_TARGET_TO_GROUP              ALL, AGENCY, OWNER Manage Target Groups
MANAGE_GROUP_SCHEDULE            ALL, AGENCY, OWNER Manage Target Groups
MANAGE_GROUP_OVERRIDES           ALL, AGENCY, OWNER Manage Target Groups
VIEW_PROFILES                    ALL, AGENCY        Manage Profiles
MANAGE_PROFILES                  ALL, AGENCY        Manage Profiles
MANAGE_REASONS                   ALL, AGENCY        Manage Rejection Reasons, Agencies, Users and Roles
MANAGE_AGENCIES                  ALL, AGENCY        Manage Rejection Reasons, Agencies, Users and Roles
MANAGE_USERS                     ALL, AGENCY        Manage Rejection Reasons, Agencies, Users and Roles
MANAGE_ROLES                     ALL, AGENCY        Manage Rejection Reasons, Agencies, Users and Roles
MANAGE_INDICATORS                ALL, AGENCY        Manage Rejection Reasons, Agencies, Users and Roles
MANAGE_FLAGS                     ALL, AGENCY        Manage Rejection Reasons, Agencies, Users and Roles
GRANT_CROSS_AGENCY_USER_ADMIN    NONE               Manage Rejection Reasons, Agencies, Users and Roles
CONFIGURE_PARAMETERS             NONE               Manage System
PERMISSION_REQUEST_TEMPLATE      ALL, AGENCY        Manage System
SYSTEM_REPORT_LEVEL_1            ALL, AGENCY        Reporting
DELETE_TASK                      OWNER              In-tray
TAKE_OWNERSHIP                   ALL, AGENCY, OWNER Ownership
GIVE_OWNERSHIP                   ALL, AGENCY, OWNER Ownership
================================ ================== ===================================================
