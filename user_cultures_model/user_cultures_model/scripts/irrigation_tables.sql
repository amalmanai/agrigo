-- Tables pour le module Irrigation (agri_go_db_corrected)
-- À exécuter si les tables systeme_irrigation et historique_irrigation n'existent pas.
-- Les tables parcelles et cultures doivent déjà exister (projet user_cultures_model).

USE agri_go_db_corrected;

-- Table systeme_irrigation (référence parcelles.id_parcelle)
CREATE TABLE IF NOT EXISTS `systeme_irrigation` (
  `id_systeme` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_parcelle` int(11) NOT NULL,
  `nom_systeme` varchar(100) NOT NULL,
  `seuil_humidite` decimal(5,2) DEFAULT NULL,
  `mode` varchar(20) NOT NULL COMMENT 'AUTO ou MANUEL',
  `statut` varchar(20) NOT NULL COMMENT 'ACTIF ou INACTIF',
  `date_creation` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_systeme`),
  KEY `fk_systeme_parcelle` (`id_parcelle`),
  CONSTRAINT `fk_systeme_parcelle` FOREIGN KEY (`id_parcelle`) REFERENCES `parcelles` (`id_parcelle`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table historique_irrigation (référence systeme_irrigation.id_systeme)
CREATE TABLE IF NOT EXISTS `historique_irrigation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_systeme` bigint(20) NOT NULL,
  `date_irrigation` timestamp NULL DEFAULT current_timestamp(),
  `duree_minutes` int(11) NOT NULL,
  `volume_eau` decimal(12,2) NOT NULL,
  `humidite_avant` decimal(5,2) DEFAULT NULL,
  `type_declenchement` varchar(20) NOT NULL COMMENT 'AUTO ou MANUEL',
  PRIMARY KEY (`id`),
  KEY `fk_historique_systeme` (`id_systeme`),
  CONSTRAINT `fk_historique_systeme` FOREIGN KEY (`id_systeme`) REFERENCES `systeme_irrigation` (`id_systeme`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
