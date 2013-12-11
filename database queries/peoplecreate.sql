CREATE TABLE People
(
userID int NOT NULL,
firstName varchar (255),
lastName varchar (255) NOT NULL,
phoneType varchar(255),
email varchar (255),
CONSTRAINT pk_userID PRIMARY KEY (userID)
)