MANTA
============

MANTA (Microbiota And pheNotype correlaTion Analysis platform) is an integrative database and analysis platform that relates microbiome and phenotypic data. MANTA is a web application that works on most of the modern web browsers.

The application was developed using the [Google Web Tookit][gwt]. The database used for store the data is [PostgreSQL][psql]. 
We use [Tomcat][tomcat] as the application container. 

An example database which contains 20 samples could be found at our [web site](https://mizuguchilab.org/manta/).

MANTA basic
------------------------
MANTA basic is developed for personal usage. Instead of processing the data and then import them into the database directly,  
we implement a data manage function, that the data could be uploaded via the user interface in the web application.
On the other hand, some tables were deprecated in order to simplify the upload process.

System requirement
------------------------
[Java][java], [Apache Tomcat][tomcat], [PostgreSQL][psql]

How to use
------------------------
 * The database schema and a script to create the necessary tables could be found in the document folder.
 * The database configuration could be found in gutflora.db.properties in the resources folder.
 * Compile the GWT project and release to Tomcat. 

Please check our web site for other information.
 
Copyright and License
------------------------
Copyright (C) 2019 [The Mizuguchi Laboratory](http://mizuguchilab.org)

MANTA is licensed under the MIT License. See [LICENSE](LICENSE.md) file for licensing information.


[psql]: http://www.postgresql.org
[java]: http://openjdk.java.net
[tomcat]: http://tomcat.apache.org/
[gwt]: http://www.gwtproject.org/
