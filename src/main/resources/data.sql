insert into Customer (id, email, firstname, lastname) values (1, 'abc001@estore.com', 'abc', '001');
insert into Customer (id, email, firstname, lastname) values (2, 'abc002@estore.com', 'abc', '002');
insert into Customer (id, email, firstname, lastname) values (3, 'abc003@estore.com', 'abc', '003');

insert into Address (id, street, city, country, customer_id) values (1, '123 Mathilda', 'Sunnyvale', 'United States', 1);
insert into Address (id, street, city, country, customer_id) values (2, '123 Evelyn', 'Sunnyvale', 'United States', 1);

insert into Product (id, name, description, price) values (1, 'iPad', 'Apple tablet device', 499.0);
insert into Product (id, name, description, price) values (2, 'MacBook Pro', 'Apple notebook', 1299.0);
insert into Product (id, name, description, price) values (3, 'Dock', 'Dock for iPhone/iPad', 49.0);

insert into Product_Attributes (attributes_key, product_id, attributes) values ('connector', 1, 'socket');
insert into Product_Attributes (attributes_key, product_id, attributes) values ('connector', 3, 'plug');

insert into Orders (id, customer_id, shippingaddress_id) values (1, 1, 2);
insert into LineItem (id, product_id, amount, order_id, price) values (1, 1, 2, 1, 499.0);
insert into LineItem (id, product_id, amount, order_id, price) values (2, 2, 1, 1, 1299.0);
