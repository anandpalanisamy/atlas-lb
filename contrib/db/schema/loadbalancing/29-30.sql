USE `loadbalancing`;

CREATE TABLE `node_type` (
  `name` varchar(32) NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into node_type values('PRIMARY');
insert into node_type values('SECONDARY');

ALTER TABLE `node` ADD COLUMN `node_type` varchar(32) NOT NULL;

UPDATE `node` SET `node_type` = 'PRIMARY' WHERE `node_type` = "";

ALTER TABLE `node` ADD
      CONSTRAINT node_type_fk
      FOREIGN KEY (node_type)
      REFERENCES node_type(name);

UPDATE `meta` SET `meta_value` = '30' WHERE `meta_key`='version';

