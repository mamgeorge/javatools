-- sample H2 tables

DROP TABLE simple IF EXISTS;
CREATE TABLE simple (

	anyid       INTEGER PRIMARY KEY NOT NULL,
	anyName     VARCHAR(40),
	anyTitle    VARCHAR(40),
	anyDate     DATE
);

DROP TABLE sample IF EXISTS;
CREATE TABLE sample (

	id       UUID PRIMARY KEY NOT NULL,
	name     VARCHAR(40),
	title    VARCHAR(40),
	phone    VARCHAR(14),
	address  VARCHAR(80),
	salary   NUMERIC(20,2),
	image    BINARY(8000),
	dateAdd  TIMESTAMP,
	dateMod  TIMESTAMP
);
