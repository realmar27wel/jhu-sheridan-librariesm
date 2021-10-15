# Pre-requisites #

The war file is currently deployed on a jetty server. It should also work on any other java server container such as Tomcat. 
It requires java 1.8 or above. 
All required libraries can be found in the `pom.xml` file. 

# Install Already Built Servlet #

The svn repo contains an already built .war servlet. You can, if you like, simply take it and install it in your HIP JBoss.

  1. Download the war file from http://horizon-holding-info-servlet.googlecode.com/svn/trunk/dist/ws.war (full version) or http://horizon-holding-info-servlet.googlecode.com/svn/trunk/dist/items.war (items-only version).
  1. Put it into your hzapp/jboss/server/default/deploy directory.
  1. Set up your Java data source (see below)
  1. Restart Jboss.

If you want to set any config properties though, you're going to need to unzip the .war file (use a standard zip utility), edit the file in config/general.properties and then rezip it back up into a .war file.

See also Setting Up Datasource below.

# Or, Install or Compile with Ant #

Alternately, you can use ant to recompile the code, package up the war file from source, and even install it in HIP for you.

Check out the project from svn, and then run "ant" in the project directory to compile, or "ant install" to actually stick it into Jboss for you.

For items-only version, you can run "ant items-install" (or "ant items-war" to make a war file in dist/ but not install it).

A default config properties file is in src/web/config/general.properties. You can edit this file before building to set properties.

For **both** tasks, however, you're going to have to tell ant where your JBoss is located (necessary for compiling, so it can find a javax.jar file needed, and for installing so it knows where to install!).  Either edit the build.xml to set the jboss.location property, or set it on the command line, to the location of your HIP 'jboss' directory, for instance:

ant install -Djboss.location=/opt/dynix/hzapp/jboss

See also Setting Up Datasource below.

# Setting up the data source #

Either way, you're going to have to configure a data source in JBoss so the servlet can find your Horizon instance. By default the servlet uses a datasource named "horizon-extras".  The datasource name the servlet looks for can be changed in config/general.properties

You would typically set up a data source by creating or adding to a file named 11sybase-ds.xml (or 11mssql-ds.xml) in your jboss/server/default/deploy directory. Look at the existing Horizon data source in 10sybase-ds.xml (or 10-mssq-ds.xml) as a model.

Alternately, you could try setting the servlet to use your existing Horizon data source that HIP uses, but that may or may not be a good idea. Seems safer to keep them separate.
