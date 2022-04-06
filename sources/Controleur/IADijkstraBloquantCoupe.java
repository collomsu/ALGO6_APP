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
import java.util.Set;
import java.util.logging.Logger;
import java.awt.Point;



//IA appellée lorsque le joueur décide de laisser l'intelligence artificielle lui donner une solution pour le niveau.
//Si le joueur était dans une situation bloquante, le niveau est rechargé.
//Si une fois rechargé, le niveau n'a pas de solution, un message est affiché au joueur pour le lui indiquer.
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

		this.positionPousseur = positionPousseur;
		this.positionsCaisses = positionsCaisses;
	}

	//Constructeur retournant la configuration de niveau correspondante à l'objet Niveau passé en paramètre
	public ConfigurationNiveau(Niveau niveau)
	{
		//Récupération du logger
		logger = Configuration.instance().logger();

		//Lecture des informations du niveau
		//Lecture de la position du pousseur
		positionPousseur = new Point(niveau.colonnePousseur(), niveau.lignePousseur());

		//Lecture de la position des caisses
		positionsCaisses = new ArrayList<Point>();

		//i = abscisses = axe x = colonnes
		//j = ordonnées = axe y = lignes
		int i = 0, j;
		while(i < niveau.colonnes())
		{
			j = 0;

			while(j < niveau.lignes())
			{
				if(niveau.aCaisseXY(i, j))
				{
					positionsCaisses.add(new Point(i, j));
				}

				j = j + 1;
			}

			i = i + 1;
		}
	}

	@Override
	public ConfigurationNiveau clone() {
		return new ConfigurationNiveau(this.positionPousseur, this.positionsCaisses);
	}

	@Override
	public boolean equals(Object obj) {
		boolean retour = false;

		if (obj == null) {
            retour = false;
        }
		else
		{
			if (obj.getClass() != this.getClass()) {
				retour = false;
			}
			else
			{
				ConfigurationNiveau objetCompare = (ConfigurationNiveau) obj;

				if(this.positionPousseur.equals(objetCompare.positionPousseur)
				   && this.positionsCaisses.equals(objetCompare.positionsCaisses))
				{
					retour = true;
				}
			}
		}

		return retour;
	}

	@Override
	public int hashCode() {
		return positionPousseur.hashCode() + positionsCaisses.hashCode();
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

		Point coordonneesPousseurApresDeplacement = coordonneesApresDeplacement(this.positionPousseur, direction);
		Point coordonneesCaisseApresDeplacement;


		//Un déplacement du pousseur est possible si:
		//-Il n'y a rien dans la direction du déplacement
		if(niveau.aMurXY(coordonneesPousseurApresDeplacement.x, coordonneesPousseurApresDeplacement.y) == false
			&& this.estCaissePresente(coordonneesPousseurApresDeplacement.x, coordonneesPousseurApresDeplacement.y) == false)
		{
			retour = true;
		}
		else
		{
			//-Ou si il y a une caisse avec rien derrière
			if(this.estCaissePresente(coordonneesPousseurApresDeplacement.x, coordonneesPousseurApresDeplacement.y) == true)
			{
				//Si il y a une caisse, on regarde ce qu'il y a derrière
				coordonneesCaisseApresDeplacement = coordonneesApresDeplacement(coordonneesPousseurApresDeplacement, direction);
				
				//Si il n'y a rien derrière la caisse, elle peut être déplacée
				if(niveau.aMurXY(coordonneesCaisseApresDeplacement.x, coordonneesCaisseApresDeplacement.y) == false
				   && this.estCaissePresente(coordonneesCaisseApresDeplacement.x, coordonneesCaisseApresDeplacement.y) == false)
				{
					retour = true;
				}
			}
		}

		return retour;
	}

	//Fonction indiquant si la configuration courrante est bloquante.
	//Une configuration est considérée comme bloquante si:
	//-Une caisse est dans un coin
	// OU
	//-Une caisse est collée contre un mur.
	//-Ladite caisse ne peut être décollée du mur.
	//-Il n'y a contre ce mur pas assez de destinations pour le nombre de caisses qui y sont collées
	public boolean estConfigurationBloquante(Niveau niveau)
	{
		boolean retour = false;

		int i = 0, j;
		boolean caisseColleeAMur, caissePeutEtreDecolleeMur;
		int directionMur;

		while (retour == false && i < this.positionsCaisses.size())
		{
			//On regarde si une caisse est collée à un mur
			caisseColleeAMur = false;

			directionMur = this.estCaisseColleeAMur(i, niveau);

			if(directionMur != -1)
			{
				caisseColleeAMur = true;
			}

			//Si une caisse est collée à un mur, on regarde si on peut décoller ladite caisse du mur
			if(caisseColleeAMur = true)
			{
				//Couple de vecteurs utilisé pour calculer si la caisse peut être décollé du mur
				int vecteurMurX = 0, vecteurMurY = 0;
				//->Si ce couple = (1, 0), on regarde si l'on peut décoller la caisse d'un supposé mur à sa droite

				if(directionMur == DROITE)
				{
					vecteurMurX = 1;
					vecteurMurY = 0;
				}
				else
				{
					if(directionMur == BAS)
					{
						vecteurMurX = 0;
						vecteurMurY = 1;
					}
					else
					{
						if(directionMur == GAUCHE)
						{
							vecteurMurX = -1;
							vecteurMurY = 0;
						}
						else
						{
							if(directionMur == HAUT)
							{
								vecteurMurX = 0;
								vecteurMurY = -1;
							}
						}
					}
				}

				//Une caisse peut être décollée d'un mur si en la poussant à l'une des extrémités elle n'y est plus collée
				//La caisse doit donc pouvoir être poussée en direction de l'extrémité pour pouvoir l'atteindre
				//->Le pousseur doit pouvoir se faufiler derrière elle

				caissePeutEtreDecolleeMur = false;

				//On regarde si le pousseur peut se faufiler derrière la caisse pour la pousser vers la première extrémité
				if(niveau.aMurXY(this.positionsCaisses.get(i).x - Math.abs(vecteurMurY), this.positionsCaisses.get(i).y - Math.abs(vecteurMurX)) == false
					&& this.estCaissePresente(this.positionsCaisses.get(i).x - Math.abs(vecteurMurY), this.positionsCaisses.get(i).y - Math.abs(vecteurMurX)))
				{
					//On regarde si en poussant la caisse vers la première extrémité on finirait par la décoller du mur
					j = 1;

					while (caissePeutEtreDecolleeMur == false
						   && (this.positionsCaisses.get(i).x - (Math.abs(vecteurMurY) * j)) >= 0
						   && (this.positionsCaisses.get(i).y - (Math.abs(vecteurMurX) * j)) >= 0)
					{
						//Si la caisse est décollée du mur
						if(niveau.aMurXY(this.positionsCaisses.get(i).x + vecteurMurX - (Math.abs(vecteurMurY) * j),
										 this.positionsCaisses.get(i).y + vecteurMurY - (Math.abs(vecteurMurX) * j)) == false)
						{
							caissePeutEtreDecolleeMur = true;
						}
						else
						{
							j = j + 1;
						}
					}
				}

				//Si on ne peut pas décoller la caisse du mur en la poussant vers la première extrémité
				//On regarde si le pousseur peut se faufiler derrière la caisse pour la pousser vers la seconde extrémité
				if(caissePeutEtreDecolleeMur == false)
				{
					//On regarde si en poussant la caisse vers la seconde extrémité on finirait par la décoller du mur
					if(niveau.aMurXY(this.positionsCaisses.get(i).x + Math.abs(vecteurMurY), this.positionsCaisses.get(i).y + Math.abs(vecteurMurX)) == false
					&& this.estCaissePresente(this.positionsCaisses.get(i).x + Math.abs(vecteurMurY), this.positionsCaisses.get(i).y + Math.abs(vecteurMurX)))
					{
						j = 1;

						while (caissePeutEtreDecolleeMur == false
							&& (this.positionsCaisses.get(i).x + (Math.abs(vecteurMurY) * j)) < niveau.colonnes()
							&& (this.positionsCaisses.get(i).y + (Math.abs(vecteurMurX) * j)) < niveau.lignes())
						{
							//Si la caisse est décollée du mur
							if(niveau.aMurXY(this.positionsCaisses.get(i).x + vecteurMurX + (Math.abs(vecteurMurY) * j),
											this.positionsCaisses.get(i).y + vecteurMurY + (Math.abs(vecteurMurX) * j)) == false)
							{
								caissePeutEtreDecolleeMur = true;
							}
							else
							{
								j = j + 1;
							}
						}
					}
				}
			}

			//Si la caisse ne peut pas être décollée du mur, on regarde si une destination est collée à ce mur

			if(caisseColleeAMur == false)
			{
				i = i + 1;
			}
		}

		return retour;
	}

	//Fonction indiquant si une caisse est présente dans les coordonnées indiquées
	public boolean estCaissePresente(int c, int l)
	{
		boolean retour = false;

		int i = 0;

		while (retour == false && i < this.positionsCaisses.size())
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

	//Fonction indiquant si la caisse de numéro numeroCaisse (indice dans la liste des caisses) est collée à un mur
	//Renvoie -1 si elle n'est collée à aucun mur, sinon renvoie la direction de ce mur
	public int estCaisseColleeAMur(int numeroCaisse, Niveau niveau)
	{
		int retour = -1;

		if(niveau.aMurXY(this.positionsCaisses.get(numeroCaisse).x + 1, this.positionsCaisses.get(numeroCaisse).y))
		{
			retour = DROITE;
		}
		else
		{
			//Si un mur est présent en bas de la caisse
			if(niveau.aMurXY(this.positionsCaisses.get(numeroCaisse).x, this.positionsCaisses.get(numeroCaisse).y + 1))
			{
				retour = BAS;
			}
			else
			{
				//Si un mur est présent à gauche de la caisse
				if(niveau.aMurXY(this.positionsCaisses.get(numeroCaisse).x - 1, this.positionsCaisses.get(numeroCaisse).y))
				{
					retour = GAUCHE;
				}
				else
				{
					//Si un mur est présent en haut de la caisse
					if(niveau.aMurXY(this.positionsCaisses.get(numeroCaisse).x, this.positionsCaisses.get(numeroCaisse).y - 1))
					{
						retour = HAUT;
					}
				}
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
			if(coordonneesPousseurApresDeplacement.x == this.positionsCaisses.get(i).x
			   && coordonneesPousseurApresDeplacement.y == this.positionsCaisses.get(i).y)
			{
				coordonneesCaisseApresDeplacement = coordonneesApresDeplacement(this.positionsCaisses.get(i), direction);
			}
			else
			{
				coordonneesCaisseApresDeplacement = (Point) this.positionsCaisses.get(i).clone();
			}

			configurationApresDeplacement.positionsCaisses.add(coordonneesCaisseApresDeplacement);

			i = i + 1;
		}

		return configurationApresDeplacement;
	}

	//Fonction regardant si l'objet ConfigurationNiveau présent est l'une des clés de la table de hachage passée
	//en parametre
	public <TypeCles> boolean estCleHashTable(Hashtable<ConfigurationNiveau, TypeCles> table)
	{
		boolean retour = false;

		Set<ConfigurationNiveau> clesTable = table.keySet();

		for(ConfigurationNiveau cle: clesTable)
		{
			if(this.equals(cle))
			{
				retour = true;
				break;
			}
		}

		return retour;
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