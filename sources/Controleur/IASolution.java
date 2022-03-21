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
import java.util.Hashtable;
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

	//Directions
	final static int DROITE = 0;
	final static int BAS = 1;
	final static int GAUCHE = 2;
	final static int HAUT = 3;

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
		int i;

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
			configurationVisitee = configurationsAVisiter.extraitTete();

			//On indique dans la table de hachage que la configuration actuelle est visitée
			tableVisiteConfigurations.get(configurationVisitee).aEteVisiteeConfiguration = true;

			//On regarde si la configuration actuelle est une solution
			i = 0;
			aEteTrouveeSolution = true;
			while(i < configurationVisitee.positionsCaisses.size() && aEteTrouveeSolution == true)
			{
				if(niveau.aBut(configurationVisitee.positionsCaisses.get(i).x, configurationVisitee.positionsCaisses.get(i).y) == false)
				{
					aEteTrouveeSolution = false;
				}

				i = i + 1;
			}

			//Si la configuration n'est pas une solution
			if(aEteTrouveeSolution == false)
			{
				logger.info("Ca passe0.");
				//Ajout à la file des configurations voisines possibles non-visitées.

				//Si l'on déplace le pousseur à droite
				if(configurationVisitee.peutPousseurSeDeplacer(DROITE, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(DROITE);

					if(tableVisiteConfigurations.get(configurationAVisiter) == null)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}

				//Si l'on déplace le pousseur en bas
				if(configurationVisitee.peutPousseurSeDeplacer(BAS, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(BAS);

					if(tableVisiteConfigurations.get(configurationAVisiter) == null)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}

				//Si l'on déplace le pousseur à gauche
				if(configurationVisitee.peutPousseurSeDeplacer(GAUCHE, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(GAUCHE);

					if(tableVisiteConfigurations.get(configurationAVisiter) == null)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}

				//Si l'on déplace le pousseur en haut
				if(configurationVisitee.peutPousseurSeDeplacer(HAUT, niveau))
				{
					configurationAVisiter = configurationVisitee.configurationApresDeplacement(HAUT);

					if(tableVisiteConfigurations.get(configurationAVisiter) == null)
					{
						tableVisiteConfigurations.put(configurationAVisiter, new InfoVisiteConfigurationNiveau(configurationVisitee, false));
						configurationsAVisiter.insereQueue(configurationAVisiter);
					}
				}
			}
			//Si la configuration actuelle est une solution
			else
			{
				logger.info("Ca passe1.");
				aEteTrouveeSolution = true;

				//Reconstruction de la solution à partir de l'évolution de la position du joueur
				Sequence<ConfigurationNiveau> sequenceConfigurationsSolution = Configuration.instance().nouvelleSequence();

				while(configurationVisitee != null)
				{
					sequenceConfigurationsSolution.insereQueue(configurationVisitee);
					configurationVisitee = tableVisiteConfigurations.get(configurationVisitee).configurationPrecedente;
				}

				Coup coup;
				ConfigurationNiveau configurationPrecedenteSequence = sequenceConfigurationsSolution.extraitTete();
				ConfigurationNiveau configurationActuelleSequence;
				
				while(sequenceConfigurationsSolution.estVide() == false)
				{
					configurationActuelleSequence = sequenceConfigurationsSolution.extraitTete();

					//Si le coup est d'aller à droite
					if(configurationActuelleSequence.positionPousseur.x == configurationPrecedenteSequence.positionPousseur.x + 1)
					{
						coup = niveau.creerCoup(1, 0);
					}
					else
					{
						//Si le coup est d'aller en bas
						if(configurationActuelleSequence.positionPousseur.y == configurationPrecedenteSequence.positionPousseur.y + 1)
						{
							coup = niveau.creerCoup(0, -1);
						}
						else
						{
							//Si le coup est d'aller à gauche
							if(configurationActuelleSequence.positionPousseur.x == configurationPrecedenteSequence.positionPousseur.x - 1)
							{
								coup = niveau.creerCoup(-1, 0);
							}
							//Si le coup est d'aller en haut
							else
							{
								coup = niveau.creerCoup(0, 1);
							}
						}
					}

					solution.insereQueue(coup);
				}
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
		//Cette fonction commence par essayer de trouver une solution au niveau dans la configuration dans laquelle le joueur
		//l'a laissé. Si cette situation est bloquante, le niveau est rechargé.
		//Si une fois rechargé, le niveau n'a toujours pas de solution, un message est affiché pour l'indiquer.
		Sequence<Coup> resultat = this.TrouverSolution();

		if(resultat == null)
		{
			logger.info("La configuration actuelle du niveau est bloquante, rechargerment du niveau pour la recherche d'une solution à partir du niveau initial.");


			if(resultat == null)
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
	//Niveau coordonnées, on va de gauche à droite pour l'axe x et on va de haut en bas pour l'axe y.

	public Point positionPousseur;
	public ArrayList<Point> positionsCaisses;

	Logger logger;

	//Directions dans lesquelles le pousseur peut se déplacer
	final static int DROITE = 0;
	final static int BAS = 1;
	final static int GAUCHE = 2;
	final static int HAUT = 3;

	//Constructeurs
	public ConfigurationNiveau(Point positionPousseur, ArrayList<Point> positionsCaisses)
	{
		//Récupération du logger
		logger = Configuration.instance().logger();

		//Clonage des paramètres
		this.positionPousseur = (Point) positionPousseur.clone();
		this.positionsCaisses = new ArrayList<Point>();
    	for (Point positionCaisse : positionsCaisses) {
			this.positionsCaisses.add((Point) positionCaisse.clone());
		}
	}

	//Constructeur retournant la configuration de niveau correspondante à l'objet Niveau passé en paramètre
	public ConfigurationNiveau(Niveau niveau)
	{
		//Récupération du logger
		logger = Configuration.instance().logger();

		//Lecture des informations du niveau
		//Lecture de la position du pousseur
		positionPousseur = new Point(niveau.colonnePousseur(), niveau.lignePousseur());

		logger.info("Pousseur: (" + this.positionPousseur.x + ", " + this.positionPousseur.y + ")");


		//Lecture de la position des caisses
		positionsCaisses = new ArrayList<Point>();

		//i = abscisses = axe x = colonnes
		//j = ordonnées = axe y = lignes
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

	//Fonction utile pour le calcul de coordonnées
	private Point coordonneesApresDeplacement(Point coordonnesObjetDeplace, int direction)
	{
		Point coordonneesApresDeplacement = new Point(0, 0);

		if(direction == DROITE)
		{
			coordonneesApresDeplacement.x = coordonnesObjetDeplace.x + 1;
			coordonneesApresDeplacement.y = coordonnesObjetDeplace.y;
		}
		else
		{
			if(direction == BAS)
			{
				coordonneesApresDeplacement.x = coordonnesObjetDeplace.x;
				coordonneesApresDeplacement.y = coordonnesObjetDeplace.y + 1;
			}
			else
			{
				if(direction == GAUCHE)
				{
					coordonneesApresDeplacement.x = coordonnesObjetDeplace.x - 1;
					coordonneesApresDeplacement.y = coordonnesObjetDeplace.y;
				}
				else
				{
					if(direction == HAUT)
					{
						coordonneesApresDeplacement.x = coordonnesObjetDeplace.x;
						coordonneesApresDeplacement.y = coordonnesObjetDeplace.y - 1;
					}
					else
					{
						logger.severe("Direction inconnue.");
					}
				}
			}
		}

		return coordonneesApresDeplacement;
	}

	//Fonction indiquant si le pousseur de la configuration actuelle d'un niveau passé en paramètre peut se déplacer dans la
	//direction demandée (les 4 directions possibles sont définies au sommet de la classe)
	//Cette fonction n'est pas appellable directement depuis l'extérieur, il faut passer par l'une de ses spécialisations
	//dans une direction.
	public boolean peutPousseurSeDeplacer(int direction, Niveau niveau)
	{
		boolean retour = false;

		Point coordonneesApresDeplacement = coordonneesApresDeplacement(this.positionPousseur, direction);

		//Un déplacement du pousseur est possible si:
		//-Il n'y a rien dans la direction du déplacement
		//-Ou si il y a une caisse avec rien derrière

		//Dans un objet ConfigurationNiveau, l'axe des ordonnées (y) part du bas alors que dans un objet Niveay, il part du haut.
		//->On doit donc recalculer les positions sur l'axe des ordonnées
		if((niveau.aMur(coordonneesApresDeplacement.x, coordonneesApresDeplacement.y) == false
			&& this.estCaissePresente(coordonneesApresDeplacement.x, coordonneesApresDeplacement.y) == false)
			|| (this.estCaissePresente(coordonneesApresDeplacement.x, coordonneesApresDeplacement.y) == true
				&& niveau.aMur(coordonneesApresDeplacement.x, coordonneesApresDeplacement.y) == false
				&& this.estCaissePresente(coordonneesApresDeplacement.x, coordonneesApresDeplacement.y) == false))
		{
			retour = true;
		}

		return retour;
	}

	//Fonction indiquant si une caisse est présente dans les coordonnées indiquées
	public boolean estCaissePresente(int c, int l)
	{
		boolean retour = false;
		int i = 0;

		while (i < this.positionsCaisses.size() && retour == false)
		{
			if(this.positionsCaisses.get(i).x == c && this.positionsCaisses.get(i).y == l)
			{
				retour = true;
			}
			else
			{
				i = i + 1;
			}
		}

		return retour;
	}

	//Fonction retournant la configuration après un déplacement du pousseur.
	//->un test de la possibilité de cette configuration a du être effectué au préalable
	public ConfigurationNiveau configurationApresDeplacement(int direction)
	{
		ConfigurationNiveau configurationApresDeplacement;

		Point coordonneesPousseurApresDeplacement = coordonneesApresDeplacement(this.positionPousseur, direction);
		Point coordonneesCaisseApresDeplacement;


		configurationApresDeplacement = new ConfigurationNiveau(coordonneesPousseurApresDeplacement,
																new ArrayList<Point>());

		int i = 0;

		while (i < this.positionsCaisses.size())
		{
			coordonneesCaisseApresDeplacement = coordonneesApresDeplacement(this.positionsCaisses.get(i), direction);

			if(coordonneesCaisseApresDeplacement.x == coordonneesPousseurApresDeplacement.x
				&& coordonneesCaisseApresDeplacement.y == coordonneesPousseurApresDeplacement.y)
			{
				configurationApresDeplacement.positionsCaisses.add(new Point(coordonneesCaisseApresDeplacement.x, coordonneesCaisseApresDeplacement.y));
			}
			else
			{
				configurationApresDeplacement.positionsCaisses.add((Point) this.positionsCaisses.get(i).clone());
			}

			i = i + 1;
		}

		return configurationApresDeplacement;
	}
}


//Classe utilisée par IASolution et contenant deux attributs indiquant utiles dans la recherche de la solution d'un niveau
//de Sokoban.
class InfoVisiteConfigurationNiveau {
	public ConfigurationNiveau configurationPrecedente;
	public boolean aEteVisiteeConfiguration;

	public InfoVisiteConfigurationNiveau(ConfigurationNiveau configurationPrecedente, boolean aEteVisiteeConfiguration)
	{
		this.configurationPrecedente = configurationPrecedente;
		this.aEteVisiteeConfiguration = aEteVisiteeConfiguration;
	}
}