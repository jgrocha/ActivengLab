## PostgreSQL database

```
CREATE SCHEMA activenglabs;
  
CREATE TABLE activenglabs.sensor
(
  sensorid integer NOT NULL,
  address character varying(40) NOT NULL,
  location character varying(80) NOT NULL,
  installdate timestamp with time zone NOT NULL DEFAULT now(),
  sensortype character varying(40) NOT NULL,
  metric integer NOT NULL DEFAULT 1,
  calibrated integer NOT NULL DEFAULT 0,
  quantity character(1) NOT NULL DEFAULT 'T'::bpchar,
  decimalplaces integer NOT NULL DEFAULT 3,
  cal_a double precision NOT NULL DEFAULT 0,
  cal_b double precision NOT NULL DEFAULT 1,
  read_interval integer NOT NULL DEFAULT 2000,
  record_sample integer NOT NULL DEFAULT 1,
  CONSTRAINT sensor_pkey PRIMARY KEY (sensorid, address)
);

CREATE TABLE activenglabs.temperature
(
  id serial NOT NULL,
  sensorid integer NOT NULL,
  address character varying(40) NOT NULL,
  created timestamp with time zone NOT NULL DEFAULT now(),
  value double precision NOT NULL,
  metric integer NOT NULL DEFAULT 1,
  calibrated integer NOT NULL DEFAULT 0,
  CONSTRAINT temperature_pkey PRIMARY KEY (id),
  CONSTRAINT temperature_sensor_fk FOREIGN KEY (sensorid, address)
      REFERENCES activenglabs.sensor (sensorid, address) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE activenglabs.calibration
(
  id serial NOT NULL,
  sensorid integer NOT NULL,
  address character varying(40) NOT NULL,
  created timestamp with time zone NOT NULL DEFAULT now(),
  cal_a_old double precision NOT NULL,
  cal_b_old double precision NOT NULL,
  cal_a_new double precision NOT NULL,
  cal_b_new double precision NOT NULL,
  ref_value_high double precision NOT NULL,
  ref_value_low double precision NOT NULL,
  read_value_high double precision NOT NULL,
  read_value_low double precision NOT NULL,
  CONSTRAINT calibration_pkey PRIMARY KEY (id),
  CONSTRAINT calibration_sensor_fk FOREIGN KEY (sensorid, address)
      REFERENCES activenglabs.sensor (sensorid, address) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);
```

```
insert into activenglabs.sensor (sensorid, address, location, sensortype) values (1, '30:14:12:18:06:34', 'Braga', 'PT100');
insert into activenglabs.sensor (sensorid, address, location, sensortype) values (2, '30:14:12:18:06:34', 'Barcelos', '2x220 Ohm');

insert into activenglabs.temperature (sensorid, address, value) values (1, '30:14:12:18:06:34', 26.123);

insert into activenglabs.calibration (sensorid, address, cal_a_old, cal_b_old, cal_a_new, cal_b_new, ref_value_high, ref_value_low, read_value_high, read_value_low) 
values (1, '30:14:12:18:06:34', 0, 1, 0, 1, 250, 50, 250, 50);
```
