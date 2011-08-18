USE `loadbalancing`;

ALTER TABLE `node` DROP FOREIGN KEY `node_type_fk`;
ALTER TABLE `node` DROP KEY `node_type_fk`;

ALTER TABLE `node` DROP COLUMN `node_type`;

DROP TABLE IF EXISTS `node_type`;

