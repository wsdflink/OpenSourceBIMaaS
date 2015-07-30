# BIMaaS Platform

BIMaaS is a single, powerful, flexible open source platform, which facilitates asset data collaboration and governance of BIM standards. BIM as a Service is the only enterprise grade open source platform for assest data integration and BIM governance. Gain complete visibility of all building information management data across your entire business or infrastructure projects, share asset data securely with other departments and third party suppliers - including infrastructure, operational and maintenance - and enforce BIM Level 2 compliance using BIMaaS. BIMaaS is built for asset management executives, buildings and civil managers, large infrastructure project managers and CIOs in sectors such as Rail, Aviation, Utilities, Oil and Gas and Construction.
BIMaaS platform is built on top of WSO2 product stack and BIMServer, BIMSurfer are used to manage the BIM standard and IFC data.
See full features of BIMaaS platform in the BIMaaS official web page [www.bimaas.uk].

[www.bimaas.uk]: http://bimaas.uk

#How to build from source
Since this platform is built on top of WSO2 product stack, required WSO2 products has to be downloaded and replace the configurations files which could be found in config/{product}/ folder. Then go to dev/ folder and build the whole project using maven and place the artifacts inside each products, {product}/repository/components/lib folder.
