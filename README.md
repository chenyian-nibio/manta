MANTA
============

MANTA (Microbiota And pheNotype correlaTion Analysis platform) is an integrative database and analysis platform that relates microbiome and phenotypic data. MANTA is a web application that works on most of the modern web browsers.

The application was developed using the [Google Web Tookit](http://www.gwtproject.org/). The database used for store the data is [PostgreSQL](https://www.postgresql.org/). 
We use [Tomcat](http://tomcat.apache.org/) as the application container. Some calculations rely on [R](https://www.r-project.org/) and need some R libraries including ade4 and RServ.  

An example database which contains 20 samples could be found at our [web site](http://mizuguchilab.org/manta/). 

System requirement
------------------------
Java, Apache Tomcat, PostgreSQL, R

How to use
------------------------
 * The database schema and a script to create the necessary tables could be found in the document folder.
 * Import your microbiota and phenotype data into the corresponding tables.
 * Some configuration is needed; especially those settings in gutflora.db.properties, GutFloraConfig.java.
 * Compile the GWT project and release to Tomcat.
 * More details will be available later.
 
Copyright and License
------------------------
Copyright (C) 2019 [The Mizuguchi Laboratory](http://mizuguchilab.org)

TargetMine is licensed under the MIT License. See [LICENSE](LICENSE.md) file for licensing information.
