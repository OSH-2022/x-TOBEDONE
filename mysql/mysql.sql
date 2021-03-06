DROP DATABASE DFS;
CREATE SCHEMA DFS;

use DFS;

CREATE TABLE `DEVICE` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `IP` char(20) NOT NULL DEFAULT '',
  `PORT` int NOT NULL DEFAULT 0,
  `ISONLINE` boolean NOT NULL,
  `RS` int NULL DEFAULT 0 ,
  `TIME` int NOT NULL DEFAULT 0,
  `LEFTRS` int NULL DEFAULT 0,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `FRAGMENT` (
  `ID` int NOT NULL,
  `PATH` char(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `FILE` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `NAME` char(100) NOT NULL DEFAULT '',
  `PATH` char(60) NOT NULL DEFAULT '',
  `ATTRIBUTE` char(10) NOT NULL DEFAULT 'rwxrwxrwx',
  `TIME` char(10) NOT NULL DEFAULT '',
  `NOD` int NOT NULL DEFAULT 1,
  `NOA` int NOT NULL DEFAULT 0,
  `ISFOLDER` boolean NOT NULL DEFAULT false,
  `WHOSE` char(20) NOT NULL DEFAULT '',
  `FILETYPE` char(50) NOT NULL DEFAULT '',
  `FILESIZE` int NOT NULL DEFAULT 0,
  `ISSHARE` int NOT NULL DEFAULT 0,
  `ORIGINID` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `REQUEST` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `TYPE` int NOT NULL DEFAULT 0,
  `FRAGMENTID` int NOT NULL DEFAULT 0,
  `DEVICEID` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `USER` (
`ID` int NOT NULL AUTO_INCREMENT,
`NAME` char(20) NOT NULL UNIQUE DEFAULT '',
`PASSWD` char(20) NOT NULL DEFAULT '',
`URIS` varchar(1000) NOT NULL DEFAULT '',
`TIME` int NOT NULL DEFAULT 0,
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `GROUP_ROLE` (
`ID` int NOT NULL AUTO_INCREMENT,
`NAME` char(20) NOT NULL UNIQUE DEFAULT '',
`URIS` varchar(1000) NOT NULL DEFAULT '',
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `USER_GROUP` (
`ID` int NOT NULL AUTO_INCREMENT,
`GID` int NOT NULL DEFAULT '0',
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX `idx_FILE_PATH_NAME`  ON `DFS`.`FILE` (PATH, NAME, WHOSE) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;

CREATE UNIQUE INDEX `idx_USER_NAME`  ON `DFS`.`USER` (NAME) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;



INSERT INTO `DEVICE` VALUES (1, '127.0.0.1', 9998, true, 1, 524287, 104857600);

INSERT INTO `DEVICE` VALUES (2, '127.0.0.1', 9999, true, 2, 33554431, 104807600);

INSERT INTO `DEVICE` VALUES (3, '127.0.0.1', 10000, true, 2, 0, 2097152);

INSERT INTO `DEVICE` VALUES (4, '11.12.13.14', 2480, false, 2, 0, 2097152);

INSERT INTO `DEVICE` VALUES (5, '11.87.13.33', 5556, false, 3, 0, 3145728);

INSERT INTO `DEVICE` VALUES (6, '121.37.233.322', 5012, false, 4, 0, 4194304);

INSERT INTO `USER` VALUES (1, 'xixi', '123456', 'xixi', 33554431);

INSERT INTO `USER` VALUES (2, 'haha', '888888', 'haha', 33554431);

INSERT INTO `USER` VALUES (3, 'huhu', '520520', 'huhu', 33554431);

GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' IDENTIFIED BY 'root';
