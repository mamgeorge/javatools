-- sample HSQL tables
-- no IF EXISTS, CREATE SCHEMA SA requires privilege;

CREATE TABLE simple (

	anyid       INTEGER PRIMARY KEY,
	anyName     VARCHAR(40),
	anyTitle    VARCHAR(40),
	anyDate     DATE
);

CREATE TABLE sample (

	id       VARCHAR(36) PRIMARY KEY,
	name     VARCHAR(40),
	title    VARCHAR(40),
	phone    VARCHAR(14),
	address  VARCHAR(80),
	salary   NUMERIC(20,2),
	image    BLOB(8000),
	dateAdd  TIMESTAMP,
	dateMod  TIMESTAMP
);
