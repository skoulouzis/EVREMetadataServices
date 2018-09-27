# EVREMetadataServices

This project contains the implementation of the EVREMetadata (restful) services. It also contains some java clients of the services for testing purposes. The operations which are supported over the RDF data are namely: 
- Import
- Export
- Update
- Query

# Configurations Notes

File src/main/resources/config.properties contains information about the Triplestore which is used. There exists configuration for both a Blazegraph Triplestore and a Virtuoso Triplestore. At the moment, we consider Virtuoso to the used Triplestore. In the current setting, Virtuoso hosted in CNR is used. 

If you want to use a different instance of Virtuoso the following lines must be changed frm the config.properties file. 
- virtuoso.url=
- virtuoso.username=
- virtuoso.password=
- virtuoso.port=
- virtuoso.rest.url=



The maven project requires two external jars in order to be build properly. They can be found in folder libs and they refer on the NodeService along with the Blazegraph API which is implemented on top of the Blazegraph Triplestore. Simply install these jars in the local maven repository in order to be "visible" from the pom file. For windows, you can see the file installJars.bat which shows how you can install external jars in the local maven repository. 
