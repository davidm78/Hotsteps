CREATE TABLE Dates
(
userID int NOT NULL,
usageDate date,
CONSTRAINT fk_userID FOREIGN KEY(userID) REFERENCES People(userID)
)