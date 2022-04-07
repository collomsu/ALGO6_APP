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
import Modele.NosClasses.ConfigurationNiveau;
import Modele.NosClasses.InfoVisiteConfigurationNiveau;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;
import java.awt.Point;



//IA appellée lorsque le joueur décide de laisser l'intelligence artificielle lui donner une solution en suivant un
//algorithme de Dijkstra.
class IADijkstra extends IA {
	Logger logger;
	// Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
	final static int VERT = 0x00CC00;
	final static int MARRON = 0xBB7755;

	//Directions
	final static int DROITE = 0;
	final static int BAS = 1;
	final static int GAUCHE = 2;
	final static int HAUT = 3;

	public IADijkstra() {
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
		int i;

		int numConfigurationVisitee = 1;

		Sequence<Coup> solution = Configuration.instance().nouvelleSequence();

		//File contenant les configurations rencontrées et que l'on doit visiter
		Sequence<ConfigurationNiveau> configurationsAVisiter = Configuration.instance().nouvelleSequence();

		//Table de hachage contenant le chemin ayant permi d'aboutir à chacune des configurations (visitées/à visiter)
		//ainsi qu'un bolléen indiquant si la branche de la dite configuration a déjà été visitée ou non.
		//La configuration initiale est détectable dans la table car elle a comme "configuration mère" la valeur null.
		Hashtable<ConfigurationNiveau, InfoVisiteConfigurationNiveau> tableVisiteConfigurations = new Hashtable<ConfigurationNiveau, InfoVisiteConfigurationNiveau>();
		
		//Visite des différentes configurations en partant de la configuration actuelle
		boolean aEteTrouveeSolution = false;
		ConfigurationNiveau configurationVisitee, configurationAVisiter;

		//Initialisation de la file à priorité et de la table de hachage avec la configuration initiale
		configurationAVisiter = new ConfigurationNiveau(niveau);
		tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(null, false));
		configurationsAVisiter.insereQueue(configurationAVisiter);

		while(aEteTrouveeSolution == false && configurationsAVisiter.estVide() == false)
		{
			logger.info("Visite de la configuration n°" + numConfigurationVisitee + ".");

			configurationVisitee = configurationsAVisiter.extraitTete();

			//On indique dans la table de hachage que la configuration actuelle est visitée
			tableVisiteConfigurations.get(configurationVisitee).aEteVisiteeConfiguration = true;

			//On regarde si la configuration actuelle est une solution
			i = 0;
			aEteTrouveeSolution = true;
			while(i < configurationVisitee.positionsCaisses.size() && aEteTrouveeSolution == true)
			{
				if(niveau.aButXY(configurationVisitee.positionsCaisses.get(i).x, configurationVisitee.positionsCaisses.get(i).y) == false)
				{
					aEteTrouveeSolution = false;
				}

				i = i + 1;
			}

			//Si la configuration n'est pas une solution
			if(aEteTrouveeSolution == false)
			{
				//Ajout à la file des configurations voisines possibles non-visitées.

				//Si l'on déplace le pousseur à droite
				if(configurationVisitee.peutPousseurSeDeplacer(DROITE, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(DROITE);
					
					//Si la configuration n'a pas déjà été visitée, on l'ajoute à la file des configurations à visiter
					//Demander pourquoi un simple containsKey ne fonctionne pas alors que j'ai bien Override la méthode equals de ConfigurationNiveau
					if(tableVisiteConfigurations.containsKey(configurationAVisiter) == false)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}

				//Si l'on déplace le pousseur en bas
				if(configurationVisitee.peutPousseurSeDeplacer(BAS, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(BAS);
					
					//Si la configuration n'a pas déjà été visitée, on l'ajoute à la file des configurations à visiter
					//Demander pourquoi un simple containsKey ne fonctionne pas alors que j'ai bien Override la méthode equals de ConfigurationNiveau
					if(tableVisiteConfigurations.containsKey(configurationAVisiter) == false)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}

				//Si l'on déplace le pousseur à gauche
				if(configurationVisitee.peutPousseurSeDeplacer(GAUCHE, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(GAUCHE);
					
					//Si la configuration n'a pas déjà été visitée, on l'ajoute à la file des configurations à visiter
					//Demander pourquoi un simple containsKey ne fonctionne pas alors que j'ai bien Override la méthode equals de ConfigurationNiveau
					if(tableVisiteConfigurations.containsKey(configurationAVisiter) == false)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}

				//Si l'on déplace le pousseur en haut
				if(configurationVisitee.peutPousseurSeDeplacer(HAUT, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(HAUT);
					
					//Si la configuration n'a pas déjà été visitée, on l'ajoute à la file des configurations à visiter
					//Demander pourquoi un simple containsKey ne fonctionne pas alors que j'ai bien Override la méthode equals de ConfigurationNiveau
					if(tableVisiteConfigurations.containsKey(configurationAVisiter) == false)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}
			}
			//Si la configuration actuelle est une solution
			else
			{
				aEteTrouveeSolution = true;

				//Reconstruction de la solution à partir de l'évolution de la position du joueur
				Sequence<ConfigurationNiveau> sequenceConfigurationsSolution = Configuration.instance().nouvelleSequence();

				while(configurationVisitee != null)
				{
					sequenceConfigurationsSolution.insereTete(configurationVisitee);
					
					configurationVisitee = tableVisiteConfigurations.get(configurationVisitee).configurationPrecedente;
				}
				
				//Construction de la séquence de coups
				Coup coup;
				ConfigurationNiveau configurationPrecedenteSequence = sequenceConfigurationsSolution.extraitTete();
				ConfigurationNiveau configurationActuelleSequence;

				//On se sert d'une copie du niveau pour construire cette séquence de coup (il faut une référence de niveau à jour pour utiliser la fonction
				//creerCoup().)
				Niveau copieNiveau = niveau.clone();
				
				while(sequenceConfigurationsSolution.estVide() == false)
				{
					configurationActuelleSequence = sequenceConfigurationsSolution.extraitTete();

					//Calcul du coup en fonction de la différence de position du pousseur) qu'il y a entre les deux configurations
					//C'est la fonction creerCoup qui ne marche pas, regarder logs
					coup = copieNiveau.creerCoupXY(configurationActuelleSequence.positionPousseur.x - configurationPrecedenteSequence.positionPousseur.x,
											configurationActuelleSequence.positionPousseur.y - configurationPrecedenteSequence.positionPousseur.y);

					solution.insereQueue(coup);
					
					//Mise à jour du niveau
					copieNiveau.jouer(coup);

					configurationPrecedenteSequence = configurationActuelleSequence;
				}
			}

			if(aEteTrouveeSolution == false)
			{
				numConfigurationVisitee = numConfigurationVisitee + 1;
			}
		}

		//Si aucune solution n'a été trouvée
		if(aEteTrouveeSolution == false)
		{
			solution = null;
		}

		return solution;
	}

	@Override
	public Sequence<Coup> joue() {
		Sequence<Coup> resultat = this.TrouverSolution();

		if(resultat == null)
		{
			logger.info("Le niveau ou la configuration actuelle de ce dernier n'a pas de solution.");
		}
		
		return resultat;
	}

	@Override
	public void finalise() {
		logger.info("Fin de traitement du niveau par l'IA");
	}
}