OCDS Scraper
============

Proyecto para extraer los datos de opendata implementado en el portal de Contrataciones Publicas ([DNCP]).

[DNCP]: https://www.contrataciones.gov.py/datos/api/v2/
[mongodb]: https://www.mongodb.com/download-center?jmp=nav#community
[GUIA]: https://docs.mongodb.com/master/administration/install-community/
[GUI]: https://www.mongodb.com/download-center?jmp=nav#compass


Instalar la base de datos MongoDB ([mongodb])
Guía de instalación ([GUIA])
Instalar la GUI de MongoDB ([GUI])

Probado con version Java 1.8

**Pasos para probar el proyecto**
1. Crear token de autenticación de la API ([DNCP])  
2. Setear los parámetros de autenticación en el archivo py/gov/ocds/aplicacion/Aplicación.java
3. Editar los parámetros de búsqueda en el archivo py/gov/ocds/scraper/Scraper.java
4. Ejecutar la clase Scraper.java