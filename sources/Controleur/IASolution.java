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
package Controleur;

import Global.Configuration;
import Modele.Coup;
import Modele.Niveau;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.awt.Point;



//IA appellée lorsque le joueur décide de laisser l'intelligence artificielle lui donner une solution pour le niveau.
//Si le joueur était dans une situation bloquante, le niveau est rechargé.
//Si une fois rechargé, le niveau n'a pas de solution, un message est affiché au joueur pour le lui indiquer.
class IASolution extends IA {
	Logger logger;
	// Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
	final static int VERT = 0x00CC00;
	final static int MARRON = 0xBB7755;

	public IASolution() {
	}

	@Override
	public void initialise() {
		logger = Configuration.instance().logger();
		logger.info("Démarrage de l'IA sur un niveau de taille " + niveau.lignes() + "x" + niveau.colonnes());
	}

	//Retourne la séquence de coups correspondantes si une solution pour la configuration actuelle a été trouvée,
	//sinon null
	//La base sur laquelle est fondée cette fonction est l'algorithme de Dijkstra: les sommets que l'on parcourt
	//sont des configurations (position pousseur, positions caisses) possibles, une transition entre deux configurations
	//existe si un coup du pousseur effectuant cette transition est possible.
	//On utilise une file pour le parcourt de ce graphe (l'algorithme de Dijstra est un genre de parcours en largeur)
	//Pour construire une solution et afin d'éviter d'effectuer plusieurs fois le parcours de la branche d'une configuration:
	//on garde en mémoire, dans une table de hachage, la configuration précédant la configuration clé
	//ainsi qu'un bolléen indiquant si la branche de la dite configuration a déjà été visitée ou non.
	public Sequence<Coup> TrouverSolution() {
		Sequence<Coup> solution = Configuration.instance().nouvelleSequence();

		//File contenant les configurations rencontrées et que l'on doit visiter
		Sequence<Configuration> configurationsAVisiter = Configuration.instance().nouvelleSequence();

		//Table de hachage contenant le chemin ayant permi d'aboutir à chacune des configurations (visitées/à visiter)
		//ainsi qu'un bolléen indiquant si la branche de la dite configuration a déjà été visitée ou non.
		Hashtable<Configuration, >
		

		if()
		{
			solution = null;
		}

		return solution;
	}

	@Override
	public Sequence<Coup> joue() {
		//Cette fonction commence par essayer de trouver une solution au niveau dans la configuration dans laquelle le joueur
		//l'a laissé. Si cette situation est bloquante, le niveau est rechargé.
		//Si une fois rechargé, le niveau n'a toujours pas de solution, un message est affiché pour l'indiquer.
		Sequence<Coup> resultat = Configuration.instance().nouvelleSequence();
		Coup coup = null;
		boolean mur = true;
		int dL = 0, dC = 0;
		int nouveauL = 0;
		int nouveauC = 0;

		int pousseurL = niveau.lignePousseur();
		int pousseurC = niveau.colonnePousseur();
		// Mouvement du pousseur
		while (mur) {
			int direction;
			nouveauL = pousseurL + dL;
			nouveauC = pousseurC + dC;
			coup = niveau.creerCoup(dL, dC);
			if (coup == null) {
				if (niveau.aMur(nouveauL, nouveauC))
					logger.info("Tentative de déplacement (" + dL + ", " + dC + ") heurte un mur");
				else if (niveau.aCaisse(nouveauL, nouveauC))
					logger.info("Tentative de déplacement (" + dL + ", " + dC + ") heurte une caisse non déplaçable");
				else
					logger.severe("Tentative de déplacement (" + dL + ", " + dC + "), erreur inconnue");
				dL = dC = 0;
			} else
				mur = false;
		}

		// Ajout des marques
		for (int l = 0; l < niveau.lignes(); l++) {
			for (int c = 0; c < niveau.colonnes(); c++) {
				int marque = niveau.marque(l, c);
				if (marque == VERT)
					coup.marque(l, c, 0);
			}
		}
			
		coup.marque(pousseurL, pousseurC, MARRON);
		while (niveau.estOccupable(nouveauL, nouveauC)) {
			int marque = niveau.marque(nouveauL, nouveauC);
			if (marque == 0)
				coup.marque(nouveauL, nouveauC, VERT);
			nouveauL += dL;
			nouveauC += dC;
		}
		resultat.insereQueue(coup);

		if(true)
		{
			logger.info("La configuration actuelle du niveau est bloquante, rechargerment du niveau.");

			if(true)
			{

			}
			else
			{
				logger.info("La configuration actuelle du niveau est bloquante, rechargerment du niveau.");
			}
		}
		
		return resultat;
	}

	@Override
	public void finalise() {
		logger.info("Fin de traitement du niveau par l'IA");
	}
}


//Classe utilisée par IASolution et contenant les informations d'une configuration (position joueur + positions caisses)
class ConfigurationNiveau {
	public Point positionPousseur;
	public ArrayList<Point> positionsCaisses;

	//Constructeur retournant la configuration de niveau correspondante à l'objet Niveau passé en paramètre
	public ConfigurationNiveau(Niveau niveau)
	{
		//Lecture des informations du niveau
		//Lecture de la position du pousseur
		positionPousseur = new Point(niveau.lignePousseur(), niveau.colonnePousseur());

		//Lecture de la position des caisses
		positionsCaisses = new ArrayList<Point>();

		//i = abscisses = axe x = colonnes
		//i = ordonnées = axe y = lignes
		int i = 0, j = 0;
		while(i < niveau.colonnes())
		{
			while(j < niveau.lignes())
			{
				if(niveau.aCaisse(i, j))
				{
					positionsCaisses.add(new Point(i, j));
				}

				j = j + 1;
			}

			i = i + 1;
		}
	}
}


//Classe utilisée par IASolution et contenant deux attributs indiquant utiles dans la recherche de la solution d'un niveau
//de Sokoban.
class InfoVisiteConfigurationNiveau {
	public Configuration configurationPrecedente;
	public boolean aEteVisiteeCOnfiguration;

	public InfoVisiteConfigurationNiveau(Configuration configurationPrecedente, boolean aEteVisiteeCOnfiguration)
	{
		this.configurationPrecedente = configurationPrecedente;
		this.aEteVisiteeCOnfiguration = aEteVisiteeCOnfiguration;
	}
}