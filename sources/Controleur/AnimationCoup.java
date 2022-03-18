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

import Modele.Coup;
import Modele.Mouvement;
import Structures.Iterateur;
import Vue.InterfaceGraphique;
import Vue.InterfaceUtilisateur;

public class AnimationCoup extends Animation {
	double vitesse;
	InterfaceUtilisateur vue;
	Coup coup;
	double progres;

	public AnimationCoup(InterfaceUtilisateur inter, Coup cp, double v) {
		super(1);
		vue = inter;
		coup = cp;
		vitesse = v;
		progres = 0;
		miseAJour();
	}

	@Override
	public void miseAJour() {
		if (!estTerminee()) {
			progres += vitesse;
			if (progres > 1)
				progres = 1;
			double facteur = progres - 1;

			Iterateur<Mouvement> it = coup.mouvements().iterateur();
			while (it.aProchain()) {
				Mouvement m = it.prochain();
				double dL = (m.versL() - m.depuisL()) * facteur;
				double dC = (m.versC() - m.depuisC()) * facteur;
				vue.decale(m.versL(), m.versC(), dL, dC);
			}
		}
	}

	@Override
	public boolean estTerminee() {
		return progres == 1;
	}
}
