/*
SQLyog Ultimate v13.1.1 (64 bit)
MySQL - 8.0.30 : Database - sistem_sekolah
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`sistem_sekolah` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `sistem_sekolah`;

/*Table structure for table `absensi` */

DROP TABLE IF EXISTS `absensi`;

CREATE TABLE `absensi` (
  `id` int NOT NULL AUTO_INCREMENT,
  `siswa_id` int DEFAULT NULL,
  `tanggal` date DEFAULT NULL,
  `status` enum('Hadir','Izin','Sakit','Alpha') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `siswa_id` (`siswa_id`),
  CONSTRAINT `absensi_ibfk_1` FOREIGN KEY (`siswa_id`) REFERENCES `siswa` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `buku` */

DROP TABLE IF EXISTS `buku`;

CREATE TABLE `buku` (
  `id` int NOT NULL AUTO_INCREMENT,
  `judul` varchar(150) DEFAULT NULL,
  `penulis` varchar(100) DEFAULT NULL,
  `jumlah` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `inventaris` */

DROP TABLE IF EXISTS `inventaris`;

CREATE TABLE `inventaris` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nama_barang` varchar(100) DEFAULT NULL,
  `lokasi` varchar(100) DEFAULT NULL,
  `jumlah` int DEFAULT NULL,
  `kondisi` enum('Baik','Rusak','Hilang') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `jadwal` */

DROP TABLE IF EXISTS `jadwal`;

CREATE TABLE `jadwal` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kelas_id` int DEFAULT NULL,
  `mapel_id` int DEFAULT NULL,
  `guru_id` int DEFAULT NULL,
  `hari` enum('Senin','Selasa','Rabu','Kamis','Jumat','Sabtu') DEFAULT NULL,
  `jam_ke` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `kelas_id` (`kelas_id`),
  KEY `mapel_id` (`mapel_id`),
  KEY `guru_id` (`guru_id`),
  CONSTRAINT `jadwal_ibfk_1` FOREIGN KEY (`kelas_id`) REFERENCES `kelas` (`id`),
  CONSTRAINT `jadwal_ibfk_2` FOREIGN KEY (`mapel_id`) REFERENCES `mapel` (`id`),
  CONSTRAINT `jadwal_ibfk_3` FOREIGN KEY (`guru_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `kelas` */

DROP TABLE IF EXISTS `kelas`;

CREATE TABLE `kelas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nama_kelas` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `mapel` */

DROP TABLE IF EXISTS `mapel`;

CREATE TABLE `mapel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nama_mapel` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `nilai` */

DROP TABLE IF EXISTS `nilai`;

CREATE TABLE `nilai` (
  `id` int NOT NULL AUTO_INCREMENT,
  `siswa_id` int DEFAULT NULL,
  `mapel_id` int DEFAULT NULL,
  `semester` varchar(20) DEFAULT NULL,
  `nilai_uh` int DEFAULT NULL,
  `nilai_uts` int DEFAULT NULL,
  `nilai_uas` int DEFAULT NULL,
  `nilai_akhir` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `siswa_id` (`siswa_id`),
  KEY `mapel_id` (`mapel_id`),
  CONSTRAINT `nilai_ibfk_1` FOREIGN KEY (`siswa_id`) REFERENCES `siswa` (`id`),
  CONSTRAINT `nilai_ibfk_2` FOREIGN KEY (`mapel_id`) REFERENCES `mapel` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `peminjaman` */

DROP TABLE IF EXISTS `peminjaman`;

CREATE TABLE `peminjaman` (
  `id` int NOT NULL AUTO_INCREMENT,
  `siswa_id` int DEFAULT NULL,
  `buku_id` int DEFAULT NULL,
  `tanggal_pinjam` date DEFAULT NULL,
  `tanggal_kembali` date DEFAULT NULL,
  `tanggal_dikembalikan` date DEFAULT NULL,
  `denda` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `siswa_id` (`siswa_id`),
  KEY `buku_id` (`buku_id`),
  CONSTRAINT `peminjaman_ibfk_1` FOREIGN KEY (`siswa_id`) REFERENCES `siswa` (`id`),
  CONSTRAINT `peminjaman_ibfk_2` FOREIGN KEY (`buku_id`) REFERENCES `buku` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `siswa` */

DROP TABLE IF EXISTS `siswa`;

CREATE TABLE `siswa` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nama` varchar(100) DEFAULT NULL,
  `nis` varchar(20) DEFAULT NULL,
  `kelas_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nis` (`nis`),
  KEY `kelas_id` (`kelas_id`),
  CONSTRAINT `siswa_ibfk_1` FOREIGN KEY (`kelas_id`) REFERENCES `kelas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nama` varchar(100) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `role` enum('guru','staff','kepala_sekolah') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
