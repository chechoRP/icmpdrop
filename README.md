# icmpdrop
OpenDaylight module that drops all ICMP packets (AD-SAL).
The application can be used as a starting point to make an AD-SAL application
for the OpenDaylight controller. 

## Requirements

+ Java 7+
+ Apache Maven
+ Eclipse (Optional but helps coding process if you want to modify this application)
+ OpenDaylight (OSGi)
  - https://nexus.opendaylight.org/content/repositories/opendaylight.snapshot/org/opendaylight/integration/distributions-base/  or
  - https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/integration/distributions-base/0.1.1/distributions-base-0.1.1-osgipackage.zip

## Installation

### Dowloading the application

    git clone https://github.com/chechoRP/icmpdrop

#### Pre-Compiled Version

Grab the .jar file that is in the `target` directory.

#### Customized Version

If you make any changes to the code, of course you will need to recompile the
bundle. To do so, just run the following command:

    mvn package

The above will update the .jar file included in the `target` directory.

### Installing Bundle

Make sure the controller is up and has loaded all the built-in bundles.
Then, stop the `simple-forwarding` since it may conflict with this bundle.

Once the OSGi console shows up:

    osgi> ss | grep simple
    osgi> stop <simple-forwarding-id>

Now install the bundle:

    osgi > install file:/path/to/the/jarfile.jar
    osgi > start <id-assigned-to-the-new-bundle>

### Check Correct Installation

In order to check that our bundle is loaded by the controller, query all the
installed bundles and check the status. Our bundle should appear at the
end of the list with an `ACTIVE` status next to it.

    osgi> ss


## Acknowledgement

The code herein presented is based on Frank Durr's blog entries about OpenDaylight (http://www.frank-durr.de/)
