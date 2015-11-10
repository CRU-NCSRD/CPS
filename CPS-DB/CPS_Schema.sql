CREATE DATABASE  IF NOT EXISTS `creativityprofilingdatabase` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `creativityprofilingdatabase`;
-- MySQL dump 10.13  Distrib 5.6.13, for Win32 (x86)
--
-- Host: cru.iit.demokritos.gr    Database: creativityprofilingdatabase
-- ------------------------------------------------------
-- Server version	5.5.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `applications`
--

DROP TABLE IF EXISTS `applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `applications` (
  `application_key` bigint(20) NOT NULL,
  PRIMARY KEY (`application_key`),
  UNIQUE KEY `application_key_UNIQUE` (`application_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `canvas`
--

DROP TABLE IF EXISTS `canvas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `canvas` (
  `exhibit_id` bigint(20) NOT NULL,
  `app` longtext,
  `canvas` longtext,
  `png` longtext,
  PRIMARY KEY (`exhibit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exhibitinstance`
--

DROP TABLE IF EXISTS `exhibitinstance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exhibitinstance` (
  `exhibit_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `type_id` int(11) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `artifact` longtext,
  `window` int(11) DEFAULT NULL,
  `language_code` varchar(45) DEFAULT NULL,
  `calculated` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`exhibit_id`),
  KEY `evidence_to_user_idx` (`user_id`),
  KEY `evidence_to_type_idx` (`type_id`),
  CONSTRAINT `exhibitinstance_to_typeofexhibit` FOREIGN KEY (`type_id`) REFERENCES `typeofexhibit` (`type_id`) ON UPDATE NO ACTION,
  CONSTRAINT `exhibitinstance_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exhibitpreferencelists`
--

DROP TABLE IF EXISTS `exhibitpreferencelists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `exhibitpreferencelists` (
  `preference_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `application_id` bigint(20) DEFAULT NULL,
  `exhibit_id` bigint(20) NOT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `window` int(11) DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  PRIMARY KEY (`preference_id`,`exhibit_id`),
  KEY `exhibitpreferencelists_to_users_idx` (`user_id`),
  KEY `exhibitpreferencelists_to_applications_idx` (`application_id`),
  KEY `exhibitpreferencelists_to_exhibits_idx` (`exhibit_id`),
  CONSTRAINT `exhibitpreferencelists_to_applications` FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_key`) ON UPDATE NO ACTION,
  CONSTRAINT `exhibitpreferencelists_to_exhibitinstance` FOREIGN KEY (`exhibit_id`) REFERENCES `exhibitinstance` (`exhibit_id`) ON UPDATE NO ACTION,
  CONSTRAINT `exhibitpreferencelists_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formalevaluationlists`
--

DROP TABLE IF EXISTS `formalevaluationlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `formalevaluationlists` (
  `fevaluation_id` bigint(20) NOT NULL,
  `application_key` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  `rank` int(11) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `window` int(11) DEFAULT NULL,
  PRIMARY KEY (`fevaluation_id`,`user_id`),
  KEY `evaluationlists_to_application_idx` (`application_key`),
  KEY `evaluationlists_to_users_idx` (`user_id`),
  CONSTRAINT `evaluationlists_to_application` FOREIGN KEY (`application_key`) REFERENCES `applications` (`application_key`) ON UPDATE NO ACTION,
  CONSTRAINT `evaluationlists_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fragments`
--

DROP TABLE IF EXISTS `fragments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fragments` (
  `fragment_id` bigint(20) NOT NULL,
  `user_id_fragment` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `exhibit_id` bigint(20) DEFAULT NULL,
  `fragment` longtext,
  `sequence` int(11) DEFAULT NULL,
  `card_text` longtext,
  PRIMARY KEY (`fragment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groups` (
  `group_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`),
  KEY `groups_to_users_idx` (`user_id`),
  CONSTRAINT `groups_to_users` FOREIGN KEY (`group_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION,
  CONSTRAINT `groups_to_users_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hastypesofexhibits`
--

DROP TABLE IF EXISTS `hastypesofexhibits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hastypesofexhibits` (
  `application_key` bigint(20) NOT NULL,
  `type_id` int(11) NOT NULL,
  PRIMARY KEY (`application_key`,`type_id`),
  KEY `type_constraint_idx` (`type_id`),
  CONSTRAINT `hastypesofexhibits_to_applications` FOREIGN KEY (`application_key`) REFERENCES `applications` (`application_key`) ON UPDATE NO ACTION,
  CONSTRAINT `hastypesofexhibits_to_typeofexhibits` FOREIGN KEY (`type_id`) REFERENCES `typeofexhibit` (`type_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `informalevaluationlists`
--

DROP TABLE IF EXISTS `informalevaluationlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `informalevaluationlists` (
  `evaluation_id` int(11) NOT NULL,
  `evaluator_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  `rank` int(11) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `window` int(11) DEFAULT NULL,
  `application_key` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`evaluation_id`,`user_id`),
  KEY `evaluation_to_users_idx` (`evaluator_id`),
  KEY `evaluation_to_users_2_idx` (`user_id`),
  KEY `evaluation_to_applications_idx` (`application_key`),
  CONSTRAINT `evaluation_to_applications` FOREIGN KEY (`application_key`) REFERENCES `applications` (`application_key`) ON UPDATE NO ACTION,
  CONSTRAINT `evaluation_to_users` FOREIGN KEY (`evaluator_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION,
  CONSTRAINT `evaluation_to_users_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `metrics`
--

DROP TABLE IF EXISTS `metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metrics` (
  `metric_id` int(11) NOT NULL,
  `type_id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`metric_id`),
  KEY `metrics_to_type_idx` (`type_id`),
  CONSTRAINT `metrics_to_types` FOREIGN KEY (`type_id`) REFERENCES `typeofexhibit` (`type_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `metricscalculations`
--

DROP TABLE IF EXISTS `metricscalculations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metricscalculations` (
  `calculation_id` int(11) NOT NULL,
  `exhibit_id` bigint(20) DEFAULT NULL,
  `metric_id` int(11) DEFAULT NULL,
  `calculation` double DEFAULT NULL,
  PRIMARY KEY (`calculation_id`),
  KEY `metric_to_evidence_idx` (`exhibit_id`),
  KEY `metric_to_metrics_idx` (`metric_id`),
  CONSTRAINT `metricscalculations_to_exhibits` FOREIGN KEY (`exhibit_id`) REFERENCES `exhibitinstance` (`exhibit_id`) ON UPDATE NO ACTION,
  CONSTRAINT `metricscalculations_to_metrics` FOREIGN KEY (`metric_id`) REFERENCES `metrics` (`metric_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `properties`
--

DROP TABLE IF EXISTS `properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `properties` (
  `property_id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`property_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `typeofexhibit`
--

DROP TABLE IF EXISTS `typeofexhibit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `typeofexhibit` (
  `type_id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userproperties`
--

DROP TABLE IF EXISTS `userproperties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userproperties` (
  `user_id` bigint(20) NOT NULL,
  `property_id` int(11) NOT NULL,
  `value` double DEFAULT '0',
  `window` int(11) DEFAULT NULL,
  `timestamp` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`property_id`,`timestamp`),
  KEY `property_to_properties_idx` (`property_id`),
  CONSTRAINT `userproperties_to_properties` FOREIGN KEY (`property_id`) REFERENCES `properties` (`property_id`) ON UPDATE NO ACTION,
  CONSTRAINT `userproperties_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `application_key` bigint(20) NOT NULL,
  `isgroup` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`),
  KEY `user_to_application_idx` (`application_key`),
  CONSTRAINT `users_to_applications` FOREIGN KEY (`application_key`) REFERENCES `applications` (`application_key`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `weights`
--

DROP TABLE IF EXISTS `weights`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weights` (
  `weight_id` int(11) NOT NULL,
  `application_id` bigint(20) DEFAULT NULL,
  `metric_id` int(11) DEFAULT NULL,
  `value` double DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `window` int(11) DEFAULT NULL,
  PRIMARY KEY (`weight_id`),
  KEY `weights_to_applications_idx` (`application_id`),
  KEY `weights_to_metrics_idx` (`metric_id`),
  CONSTRAINT `weights_to_applications` FOREIGN KEY (`application_id`) REFERENCES `applications` (`application_key`) ON UPDATE NO ACTION,
  CONSTRAINT `weights_to_metrics` FOREIGN KEY (`metric_id`) REFERENCES `metrics` (`metric_id`) ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `windows`
--

DROP TABLE IF EXISTS `windows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `windows` (
  `window_id` int(11) NOT NULL,
  `timestamp` bigint(20) NOT NULL,
  `current_window` tinyint(4) DEFAULT NULL,
  `window` int(11) DEFAULT NULL,
  PRIMARY KEY (`window_id`,`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'creativityprofilingdatabase'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-11-10 17:07:17
