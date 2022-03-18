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
import Structures.Sequence;

import java.util.Random;
import java.util.logging.Logger;

class IATeleportations extends IA {
	Random r;
	Logger logger;

	public IATeleportations() {
		r = new Random();
	}

	@Override
	public void initialise() {
		logger = Configuration.instance().logger();
		logger.info("Cheat : téléportations aléatoires activé !");
	}

	@Override
	public Sequence<Coup> joue() {
		Sequence<Coup> resultat = Configuration.instance().nouvelleSequence();
		int pousseurL = niveau.lignePousseur();
		int pousseurC = niveau.colonnePousseur();

		// Ici, a titre d'exemple, on peut construire une séquence de coups
		// qui sera jouée par l'AnimationJeuAutomatique
		int nb = r.nextInt(5)+1;
		logger.info("Constrution d'une séquence de " + nb + " coups");
		for (int i = 0; i < nb; i++) {
			// Mouvement du pousseur
			Coup coup = new Coup();
			boolean libre = false;
			while (!libre) {
				int nouveauL = r.nextInt(niveau.lignes());
				int nouveauC = r.nextInt(niveau.colonnes());
				if (niveau.estOccupable(nouveauL, nouveauC)) {
					logger.info("Téléportation en (" + nouveauL + ", " + nouveauC + ") !");
					coup.deplace(pousseurL, pousseurC, nouveauL, nouveauC);
					resultat.insereQueue(coup);
					pousseurL = nouveauL;
					pousseurC = nouveauC;
					libre = true;
				}
			}
		}
		return resultat;
	}

	@Override
	public void finalise() {
		logger.info("Fin des téléportations");
	}
}
