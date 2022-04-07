package Modele.NosClasses;

import Global.Configuration;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;
import java.awt.Point;

//Classe utilisée par les classes IADijkstra contenant deux attributs indiquant utiles dans la recherche de la solution d'un niveau
//de Sokoban.
public class InfoVisiteConfigurationNiveau {
	public ConfigurationNiveau configurationPrecedente;
	public boolean aEteVisiteeConfiguration;

	public InfoVisiteConfigurationNiveau(ConfigurationNiveau configurationPrecedente, boolean aEteVisiteeConfiguration)
	{
		this.configurationPrecedente = configurationPrecedente;
		this.aEteVisiteeConfiguration = aEteVisiteeConfiguration;
	}
}