package Modele.NosClasses;

import Global.Configuration;
import Modele.Niveau;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;
import java.awt.Point;

//Classe utilisée par les classes IADijkstra contenant les informations d'une configuration (position joueur + positions caisses)
public class ConfigurationNiveau {
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
	//Une configuration est considérée comme bloquante si dans cette dernière:
	//-Une caisse n'est pas sur une destination.
	//-Elle est collée contre un mur.
	//-Et si ladite caisse ne peut être déplacée pour être décollée du mur ou atteindre une destination.
	// ->Il faut que la caisse puisse être déplacée (il ne faut pas qu'elle soit dans un coin/collée à une autre caisse)
	// ->Il faut que le mur ait assez de destinations pour toutes les caisses qui sont collées à lui
	//   =>Dans le cas où ces deux conditions ne sont pas validées, la configuration est bloquante
	public boolean estConfigurationBloquante(Niveau niveau)
	{
		boolean configurationBloquante = false;

		int i = 0, j;
		boolean caisseSurDestination, caisseColleeAMur, caisseBloquee, caissePeutEtreDecolleeDuMur, caissePeutAtteindreDestination, assezDeDestinationsPourLesCaisses;
		int nbCaissesContreMur, nbDestinationsContreMur;
		int directionMur;

		while (configurationBloquante == false && i < this.positionsCaisses.size())
		{
			caisseSurDestination = false;

			//On regarde si la caisse n'est pas sur une destination
			if(niveau.aButXY(this.positionsCaisses.get(i).x, this.positionsCaisses.get(i).y))
			{
				caisseSurDestination = true;
			}

			if(caisseSurDestination == false)
			{
				//On regarde si la caisse est collée à un mur
				caisseColleeAMur = false;
				directionMur = this.estCaisseColleeAMur(i, niveau);

				if(directionMur != -1)
				{
					caisseColleeAMur = true;
				}
				
				//Si la caisse est collée à un mur, on regarde si elle peut être déplacée pour être décollée du mur ou atteindre une destination.
				//->Il faut que la caisse puisse être déplacée (il ne faut pas qu'elle soit dans un coin/collée à une autre caisse)
				//->Il faut que le mur ait assez de destinations pour toutes les caisses qui sont collées à lui
				if(caisseColleeAMur == true)
				{
					//Couple de vecteurs utilisé pour factoriser les conditions
					int vecteurMurX = 0, vecteurMurY = 0;

					//->Si ce couple = (1, 0), on regarde si l'on peut décoller la caisse d'un supposé mur à sa droite
					switch (directionMur) {
						case DROITE: {
							vecteurMurX = 1;
							vecteurMurY = 0;
							break;
						}
						case BAS: {
							vecteurMurX = 0;
							vecteurMurY = 1;
							break;
						}
						case GAUCHE: {
							vecteurMurX = -1;
							vecteurMurY = 0;
							break;
						}
						case HAUT: {
							vecteurMurX = 0;
							vecteurMurY = -1;
							break;
						}
						default:
							throw new IllegalArgumentException("Unexpected value: " + directionMur);
					}

					//On commence par contrôler si la caisse peut être déplacée
					//->Il ne faut pas qu'elle soit dans un coin/collée à une autre caisse
					caisseBloquee = false;
					if(niveau.aMurXY(this.positionsCaisses.get(i).x - Math.abs(vecteurMurY), this.positionsCaisses.get(i).y - Math.abs(vecteurMurX)) == true //Côté 1
					   || this.estCaissePresente(this.positionsCaisses.get(i).x - Math.abs(vecteurMurY), this.positionsCaisses.get(i).y - Math.abs(vecteurMurX)) == true
					   
					   || niveau.aMurXY(this.positionsCaisses.get(i).x + Math.abs(vecteurMurY), this.positionsCaisses.get(i).y + Math.abs(vecteurMurX)) == true //Côté 2
					   || this.estCaissePresente(this.positionsCaisses.get(i).x + Math.abs(vecteurMurY), this.positionsCaisses.get(i).y + Math.abs(vecteurMurX)) == true)
					{
						caisseBloquee = true;
					}

					//Si la caisse est bloquée (sur une case qui n'est pas une destination)
					if(caisseBloquee == true)
					{
						configurationBloquante = true;
					}
					//Si la caisse peut être déplacée
					else
					{
						//On regarde si la caisse peut être déplacée pour être décollée du mur ou atteindre une destination.
						caissePeutEtreDecolleeDuMur = false;
						caissePeutAtteindreDestination = false;
						
						//Une caisse peut être décollée d'un mur si en la poussant à l'une des extrémités elle n'y est plus collée
						//Une caisse peut atteindre une destination si en la poussant vers l'une des extrémités elle peut en atteindre une


						//On regarde si en poussant la caisse vers la première extrémité on finirait par la décoller du mur/la placer sur une destination
						j = 1;

						while (caissePeutEtreDecolleeDuMur == false && caissePeutAtteindreDestination == false
							&& (this.positionsCaisses.get(i).x - (Math.abs(vecteurMurY) * j)) >= 0
							&& (this.positionsCaisses.get(i).y - (Math.abs(vecteurMurX) * j)) >= 0)
						{
							//Si la caisse est décollée du mur
							if(niveau.aMurXY(this.positionsCaisses.get(i).x + vecteurMurX - (Math.abs(vecteurMurY) * j),
												this.positionsCaisses.get(i).y + vecteurMurY - (Math.abs(vecteurMurX) * j)) == false)
							{
								caissePeutEtreDecolleeDuMur = true;
							}
							else
							{
								//Si la caisse a atteint une destination
								if(niveau.aButXY(this.positionsCaisses.get(i).x - (Math.abs(vecteurMurY) * j),
													this.positionsCaisses.get(i).y - (Math.abs(vecteurMurX) * j)))
								{
									caissePeutAtteindreDestination = true;
								}
								else
								{
									j = j + 1;
								}
							}
						}


						//Si en poussant la caisse vers la première extrémité la situation est bloquante
						if(caissePeutEtreDecolleeDuMur == false && caissePeutAtteindreDestination == false)
						{
							//On regarde si en poussant la caisse vers la première extrémité on finirait par la décoller du mur/la placer sur une destination
							j = 1;

							while (caissePeutEtreDecolleeDuMur == false && caissePeutAtteindreDestination == false
								&& (this.positionsCaisses.get(i).x + (Math.abs(vecteurMurY) * j)) < niveau.colonnes()
								&& (this.positionsCaisses.get(i).y + (Math.abs(vecteurMurX) * j)) < niveau.lignes())
							{
								//Si la caisse est décollée du mur
								if(niveau.aMurXY(this.positionsCaisses.get(i).x + vecteurMurX + (Math.abs(vecteurMurY) * j),
												this.positionsCaisses.get(i).y + vecteurMurY + (Math.abs(vecteurMurX) * j)) == false)
								{
									caissePeutEtreDecolleeDuMur = true;
								}
								else
								{
									//Si la caisse a atteint une destination
									if(niveau.aButXY(this.positionsCaisses.get(i).x + (Math.abs(vecteurMurY) * j),
													this.positionsCaisses.get(i).y + (Math.abs(vecteurMurX) * j)))
									{
										caissePeutAtteindreDestination = true;
									}
									else
									{
										j = j + 1;
									}
								}
							}
						}

						//Si la caisse ne peut être ni décollée du mur, ni atteindre une destination
						if(caissePeutAtteindreDestination == false && caissePeutEtreDecolleeDuMur == false)
						{
							configurationBloquante = true;
						}
						else
						{
							//On termine en contrôlant, dans le cas où la caisse peut atteindre une destination mais ne peut être décollée du mur
							//Si assez de destinations sont placées contre le mur pour toutes les caisses qui sont collées à lui
							if(caissePeutAtteindreDestination == true && caissePeutEtreDecolleeDuMur == false)
							{
								assezDeDestinationsPourLesCaisses = false;
								nbCaissesContreMur = 0;
								nbDestinationsContreMur = 0;

								//Extremité 1 + position actuelle
								j = 0;

								while ((this.positionsCaisses.get(i).x - (Math.abs(vecteurMurY) * j)) >= 0
									&& (this.positionsCaisses.get(i).y - (Math.abs(vecteurMurX) * j)) >= 0)
								{
									//Si la case actuelle est une caisse
									if(this.estCaissePresente(this.positionsCaisses.get(i).x - (Math.abs(vecteurMurY) * j),
															this.positionsCaisses.get(i).y - (Math.abs(vecteurMurX) * j)))
									{
										nbCaissesContreMur = nbCaissesContreMur + 1;
									}

									//Si la case actuelle est une destination
									if(niveau.aButXY(this.positionsCaisses.get(i).x - (Math.abs(vecteurMurY) * j),
													this.positionsCaisses.get(i).y - (Math.abs(vecteurMurX) * j)))
									{
										nbDestinationsContreMur = nbDestinationsContreMur + 1;
									}
									
									j = j + 1;
								}

								//Extremité 2
								j = 0;

								while ((this.positionsCaisses.get(i).x + (Math.abs(vecteurMurY) * j)) < niveau.colonnes()
									&& (this.positionsCaisses.get(i).y + (Math.abs(vecteurMurX) * j)) < niveau.lignes())
								{
									//Si la case actuelle est une caisse
									if(this.estCaissePresente(this.positionsCaisses.get(i).x + (Math.abs(vecteurMurY) * j),
															this.positionsCaisses.get(i).y + (Math.abs(vecteurMurX) * j)))
									{
										nbCaissesContreMur = nbCaissesContreMur + 1;
									}

									//Si la case actuelle est une destination
									if(niveau.aButXY(this.positionsCaisses.get(i).x + (Math.abs(vecteurMurY) * j),
													this.positionsCaisses.get(i).y + (Math.abs(vecteurMurX) * j)))
									{
										nbDestinationsContreMur = nbDestinationsContreMur + 1;
									}
									
									j = j + 1;
								}


								//Si il n'y a pas assez de destinations contre le mur pour le nombre de caisses qui y sont collées
								if(nbCaissesContreMur == nbDestinationsContreMur)
								{
									assezDeDestinationsPourLesCaisses = true;
								}

								//Si la caisse ne peut être décollée du mur, peut atteindre une destination mais il y a trop de caisses
								//contre le mur pour le nombre de destinations collées à lui
								if(assezDeDestinationsPourLesCaisses == false)
								{
									configurationBloquante = true;
								}
							}
						}
					}
				}
			}
			
			
			if(configurationBloquante == false)
			{
				i = i + 1;
			}
		}

		return configurationBloquante;
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