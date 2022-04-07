package Modele.NosClasses;

import Global.Configuration;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;
import java.awt.Point;

//Classe utilisée par les classes IADijkstra contenant deux attributs donnant des informations sur les configurations clés
//de la table de hachage utilisée par les algorithmes de recherche de solution.
//-configurationPrecedente est utilisé pour indiquer la configuration ayant mené à la configuration clé dans la 
// table de hachage.
//-aEteVisiteeConfiguration indique si la configuration clé a été visitée.
public class InfoVisiteConfigurationNiveau {
	public ConfigurationNiveau configurationPrecedente;
	public boolean aEteVisiteeConfiguration;

	public InfoVisiteConfigurationNiveau(ConfigurationNiveau configurationPrecedente, boolean aEteVisiteeConfiguration)
	{
		this.configurationPrecedente = configurationPrecedente;
		this.aEteVisiteeConfiguration = aEteVisiteeConfiguration;
	}
}