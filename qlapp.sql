/*
 Navicat Premium Data Transfer

 Source Server         : my
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : localhost:3307
 Source Schema         : qlapp

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 29/04/2023 18:19:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for carinfo
-- ----------------------------
DROP TABLE IF EXISTS `carinfo`;
CREATE TABLE `carinfo`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '车辆id',
  `VIN` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'VIN',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车辆类型',
  `ICCID` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ICCID',
  `TBoxID` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '终端编号',
  `driverID` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '司机ID，表示这两车是谁的',
  `notes` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用于备注是什么车',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用于存放一辆车的基本信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of carinfo
-- ----------------------------
INSERT INTO `carinfo` VALUES (1, 'LWLRMBNG5NL097992', 'EV100', '89860470192240007571', 'N206A2200598', '1', '新轻卡3号车');

-- ----------------------------
-- Table structure for driverinfo
-- ----------------------------
DROP TABLE IF EXISTS `driverinfo`;
CREATE TABLE `driverinfo`  (
  `id` int NOT NULL COMMENT 'ID',
  `driverID` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '司机ID',
  `name` varchar(100) CHARACTER SET gbk COLLATE gbk_chinese_ci NOT NULL COMMENT '姓名',
  `area` varchar(64) CHARACTER SET gbk COLLATE gbk_chinese_ci NULL DEFAULT '重庆' COMMENT '地区',
  `phone` varchar(64) CHARACTER SET gbk COLLATE gbk_chinese_ci NOT NULL COMMENT '手机号',
  `IDCard` varchar(64) CHARACTER SET gbk COLLATE gbk_chinese_ci NOT NULL COMMENT '身份证号',
  `male` tinyint(1) NULL DEFAULT NULL COMMENT '性别',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `nikeName` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `driverID`(`driverID` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用于存放司机信息，用driverID标识唯一值' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of driverinfo
-- ----------------------------
INSERT INTO `driverinfo` VALUES (1, '1', '石李城', '重庆', '15102340479', '123456789012345678', 1, 25, '阿城同学');

SET FOREIGN_KEY_CHECKS = 1;
