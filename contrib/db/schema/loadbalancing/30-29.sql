USE `loadbalancing`;

ALTER TABLE `node` DROP FOREIGN KEY `node_type_fk`;
ALTER TABLE `node` DROP KEY `node_type_fk`;

ALTER TABLE `node` DROP COLUMN `node_type`;

DROP TABLE IF EXISTS `node_type`;

update `meta` set `meta_value` = '29' where `meta_key`='version';
