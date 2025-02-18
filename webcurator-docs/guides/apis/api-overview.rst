============
API Overview
============
In general the version number in the api call is the letter 'v' followed by the latest major version number. E.g. 'v1'. It is also possible 
to use the string 'latest' to call upon the latest version of the api. 

If an api is deprecated and no longer supported then the error 404 'Not Found – the requested resource does not exist' is returned. 

Agencies
========
.. toctree::
   :maxdepth: 1

   api-agencies_GET

Authentication
==============
.. toctree::
   :maxdepth: 1

   api-authentication_POST
   api-authentication_DELETE

Authorisations
==============
.. toctree::
   :maxdepth: 1
   
   api-authorisations_GET

.. comment::
   api-authorisation_scope_GET
   
Flags
=====
.. toctree::
   :maxdepth: 1
   
   api-flags_GET.rst

Groups
======
.. toctree::
   :maxdepth: 1
   
   api-groups_GET.rst

.. toctree::
   :maxdepth: 1
   
   api-group_POST.rst
   api-group_GET.rst
   api-group_PUT.rst
   api-group_DELETE.rst

.. toctree::
   :maxdepth: 1
   
   api-group_member_POST.rst
   api-group_member_DELETE.rst

.. toctree::
   :maxdepth: 1
   
   api-group_states_GET.rst
   api-group_types_GET.rst

Harvest Agents
==============
.. toctree::
   :maxdepth: 1
   
   api-harvest-agents_GET.rst
   
Harvest Authorisations
======================
.. toctree::
   :maxdepth: 1
   
   api-harvest_authorisations_GET.rst
   api-harvest_authorisation_states_GET.rst
   
Permissions
===========
.. toctree::
   :maxdepth: 1
   
   api-permissions_GET.rst
   api-permission_GET.rst

Profiles
========
.. toctree::
   :maxdepth: 1
   
   api-profiles_GET.rst
   api-profile_states_GET.rst
   
Targets
=======
.. toctree::
   :maxdepth: 1

   api-targets_GET

.. toctree::
   :maxdepth: 1
   
   api-target_POST
   api-target_GET
   api-target_PUT
   api-target_DELETE

.. toctree::
   :maxdepth: 1
   
   api-target_states_GET
   api-target_scheduleTypes_GET
   
Target Instances
================
.. toctree::
   :maxdepth: 1

   api-target_instances_GET

.. toctree::
   :maxdepth: 1
   
   api-target_instance_GET
   api-target_instance_PUT
   api-target_instance_DELETE
   
.. toctree::
   :maxdepth: 1

   api-target_instance_PUT_archive   
   api-target_instance_PUT_abort
   api-target_instance_PUT_patch-harvest
   api-target_instance_PUT_pause
   api-target_instance_PUT_resume
   api-target_instance_PUT_start
   api-target_instance_PUT_stop

.. toctree::
   :maxdepth: 1
   
   api-target_instance_states_GET
   api-harvest_result_states_GET

   
Users
=====
.. toctree::
   :maxdepth: 1
   
   api-users_GET.rst
