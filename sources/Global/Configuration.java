/*
 * Sokoban - Encore une nouvelle version (à but pédagogique) du célèbre jeu
 * Copyright (C) 2018 Guillaume Huard
 * 
 * Ce programme est libre, vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la Licence Publique Générale GNU publiée par la
 * Free Software Foundation (version 2 ou bien toute autre version ultérieure
 * choisie par vous).
 * 
 * Ce programme est distribué car potentiellement utile, mais SANS
 * AUCUNE GARANTIE, ni explicite ni implicite, y compris les garanties de
 * commercialisation ou d'adaptation dans un but spécifique. Reportez-vous à la
 * Licence Publique Générale GNU pour plus de détails.
 * 
 * Vous devez avoir reçu une copie de la Licence Publique Générale
 * GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
 * États-Unis.
 * 
 * Contact:
 *          Guillaume.Huard@imag.fr
 *          Laboratoire LIG
 *          700 avenue centrale
 *          Domaine universitaire
 *          38401 Saint Martin d'Hères
 */
package Global;

import Structures.Sequence;
import Structures.SequenceListe;
import Structures.SequenceTableau;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {
	static Configuration instance = null;
	Properties prop;
	Logger logger;

	public static Configuration instance() {
		if (instance == null)
			instance = new Configuration();
		return instance;
	}

	public static InputStream charge(String nom) {
		// La méthode de chargement suivante ne dépend pas du système de fichier et sera
		// donc utilisable pour un .jar
		// Attention, par contre, le fichier doit se trouver dans le CLASSPATH
		return ClassLoader.getSystemClassLoader().getResourceAsStream(nom);
	}

	static void chargerProprietes(Properties p, InputStream in, String nom) {

		try {
			p.load(in);
		} catch (IOException e) {
			System.err.println("Impossible de charger " + nom);
			System.err.println(e.toString());
			System.exit(1);
		}
	}

	protected Configuration() {
		// On charge les propriétés
		InputStream in = charge("defaut.cfg");
		Properties defaut = new Properties();
		chargerProprietes(defaut, in, "defaut.cfg");
		// Il faut attendre le dernier moment pour utiliser le logger
		// car celui-ci s'initialise avec les propriétés
		String message = "Fichier de propriétés defaut.cfg chargé";
		String nom = System.getProperty("user.home") + "/.sokoban";
		try {
			in = new FileInputStream(nom);
			prop = new Properties(defaut);
			chargerProprietes(prop, in, nom);
			logger().info(message);
			logger().info("Fichier de propriétés " + nom + " chargé");
		} catch (FileNotFoundException e) {
			prop = defaut;
			logger().info(message);
		}
	}

	public <E> Sequence<E> nouvelleSequence() {
		Sequence<E> resultat;
		// On crée la fabrique de séquences
		String type = lis("Sequence");
		switch (type) {
		case "Liste":
			resultat = new SequenceListe<>();
			break;
		case "Tableau":
			resultat = new SequenceTableau<>();
			break;
		default:
			throw new NoSuchElementException("Sequences de type " + type + " non supportées");
		}
		return resultat;
	}

	public String lis(String nom) {
		String value = prop.getProperty(nom);
		if (value != null) {
			return value;
		} else {
			throw new NoSuchElementException("Propriété " + nom + " manquante");
		}
	}

	public Logger logger() {
		if (logger == null) {
			System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s : %5$s%n");
			logger = Logger.getLogger("Sokoban.Logger");
			logger.setLevel(Level.parse(lis("LogLevel")));
		}
		return logger;
	}
}
