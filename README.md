# ChatApp
This is a Real-Time Chatting Application. I use Spring WebSocket to send and receive messages without any delay. WebSocket event listener helps me to know when the user connected or disconnected to the server and use that info for chatting. I also use Redis for Session-based Authentication to save cookies. I implement cookie-building logic too. Another thing where I use Redis is saving messages temporarily in Redis in-memory db, It will help users load their messages faster from in-memory db and that will boost the performance of the Application.

## Technologies used

• [Java](https://www.java.com): Java programming language

• [Spring Boot](https://spring.io/projects/spring-boot): Most famous framework for building server applications on java

• [Spring WebSocket](https://spring.io/guides/gs/messaging-stomp-websocket): WebSocket to receive and send messages in Real-Time

• [Spring Data JPA](https://spring.io/projects/spring-data-jpa): For mapping Java objects to a relational database and relate models to each other

• [Redis](https://redis.io/): The open source, in-memory data store 

• [PostgresSQL](https://www.postgresql.org/docs/current/index.html): Very popular and battle tested Relational type of database

• [Hibernate](https://hibernate.org/): Most popular JPA implementation. Battle tested framework to work with databases from java.

