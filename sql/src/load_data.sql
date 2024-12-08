/* Replace the location to where you saved the data files*/
COPY Users
FROM '/home/csmajs/tsaka013/project/data/users.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Items
FROM '/home/csmajs/tsaka013/project/data/items.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Store
FROM '/home/csmajs/tsaka013/project/data/store.csv'
WITH DELIMITER ',' CSV HEADER;

COPY FoodOrder
FROM '/home/csmajs/tsaka013/project/data/foodorder.csv'
WITH DELIMITER ',' CSV HEADER;

COPY ItemsInOrder
FROM '/home/csmajs/tsaka013/project/data/itemsinorder.csv'
WITH DELIMITER ',' CSV HEADER;
