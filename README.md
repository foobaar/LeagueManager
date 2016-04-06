# LeagueManager
Small project to test different graph databases and different strategies of graph database bulk loading.

Prerequisites
-----------------
This project requires the following:

1. Java 8
2. Gradle
3. Local install of Neo4J

Overview
--------
Ingesting large amounts of data in any database is difficult and there are few good
resources for testing loading techniques in NOSQL databases. To help investigate
different loading strategies in different Graph database technologies, we have decided
to model the relationships of the ever popular SportsBall teams.  The data model for
SportsBall teams and their Leagues will be:

 * People (Types: Player, Coach, Fan) are associated with one or more Teams
 * Teams are play in one or more Leagues
 * Gyms are associated with one or more Leagues
 * Leagues contain Teams and Gyms and are part of a Region
 * Regions contain one or more Leagues

How to use
----------
To run the test project either run the Spring Boot application from the command line or from your IDE of choice.

|Functionality |JVM parameter |Description|
|---|---|---|
| Load All data | loadAll  |Run all loaders in the application.   |
| Load League data  | loadLeagues  | Only load league data to the application.  |
| Load Region data  | loadRegions  | Only load region data to the application. |

Sample usage: *java -jar leagueManager-0.0.1-RELEASE.jar loadLeagues loadRegions*

This will load the leagues and regions into the application.


[Project work that is still needed](./docs/TODOs.md)
