CREATE TABLE `VITAL_VALUES` (
  `id_vital_values` int(11) NOT NULL AUTO_INCREMENT,
  `DEVICE_ID` varchar(64) DEFAULT NULL,
  `METRIC_ID` varchar(64) DEFAULT NULL,
  `INSTANCE_ID` int(11) DEFAULT NULL,
  `TIME_TICK` timestamp NULL DEFAULT NULL,
  `VITAL_VALUE` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_vital_values`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
