ServiceTicketPortal:

To run Service ticket portal, run CT5106-project folder in powershell, then use: 
.\mvnw.cmd spring-boot:run

If port 8080 is in use, run:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"

RepairTicketTest tests info:
openingATicketStartsItAsOpen checks that a new ticket with a description starts as OPEN.
aBlankDescriptionIsRefused checks that blank descriptions are rejected and a ticket isnt made.
anOpenTicketCannotBeClosed checks that closing an open ticket fails, the error is “A ticket has to be resolved before it can be closed”, and the ticket stays OPEN.
aResolvedTicketCanBeClosed checks that resolving a ticket and then closing it leaves it status as CLOSED.

to run these tests use:
.\mvnw.cmd test
