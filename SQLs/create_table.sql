CREATE TABLE increments(
  table_name	varchar(20),
  column_name	varchar(20),
  increments	number(20) DEFAULT 1,
  PRIMARY KEY (table_name, column_name)
);
CREATE TABLE customer (
  id		number(20),
  name	varchar(20),
  gender	varchar(10),
  address	varchar(20),
  phone	varchar(20),
  PRIMARY KEY (id)
);
CREATE TABLE room (
  room_no		number(4),
  max_people	number(3),
  room_type	varchar(10),
  PRIMARY KEY (room_no)
);
CREATE TABLE staff(
  id		number(20),
  name	varchar(20),
  gender	varchar(10),
  address	varchar(20),
  phone	varchar(20),
  PRIMARY KEY (id)
);
CREATE TABLE reservations(
  customer_id		number(20),
  start_date	date,
  nights		number(10),
  room_no		number(4),
  staff_id		number(20),
  PRIMARY KEY (room_no, start_date),
  FOREIGN KEY (customer_id) REFERENCES customer(id),
  FOREIGN KEY (staff_id) REFERENCES staff(id)
);

INSERT INTO increments(table_name, column_name) VALUES('staff', 'id');
INSERT INTO increments(table_name, column_name) VALUES('customer', 'id');
