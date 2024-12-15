DROP INDEX loginIndex;

CREATE INDEX loginIndex 
ON Users(login);

DROP INDEX foodOrderIndex;

CREATE INDEX foodOrderIndex
ON FoodOrder(login);