MANTA
============

MANTA (Microbiota And pheNotype correlaTion Analysis platform) is an integrative database and analysis platform that relates microbiome and phenotypic data. MANTA is a web application that works on most of the modern web browsers.

The application was developed using the [Google Web Tookit][gwt]. The database used for store the data is [PostgreSQL][psql]. 
We use [Tomcat][tomcat] as the application container. Some calculations rely on [R][r-project] and need some R libraries including ade4 and RServ.  

An example database which contains 20 samples could be found at our [web site](http://mizuguchilab.org/manta/). 

System requirement
------------------------
[Java][java], [Apache Tomcat][tomcat], [PostgreSQL][psql], [R][r-project]

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

MANTA is licensed under the MIT License. See [LICENSE](LICENSE.md) file for licensing information.


[psql]: http://www.postgresql.org
[java]: http://openjdk.java.net
[tomcat]: http://tomcat.apache.org/
[r-project]: https://www.r-project.org/
[gwt]: http://www.gwtproject.org/
