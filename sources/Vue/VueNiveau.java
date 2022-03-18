package Vue;/*
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

import Global.Configuration;
import Modele.Jeu;
import Modele.Niveau;

import java.io.InputStream;

public class VueNiveau extends NiveauGraphique {
	ImageSokoban pousseur, mur, sol, caisse, but, caisseSurBut;
	Jeu j;
	int hauteurCase, largeurCase;
	// Décalage des éléments (pour pouvoir les animer)
	Vecteur[][] decalages;
	// Images du pousseur (pour l'animation)
	ImageSokoban[][] pousseurs;
	int direction, etape;

	VueNiveau(Jeu jeu) {
		j = jeu;
		j.ajouteObservateur(this);
		mur = lisImage("Mur");
		sol = lisImage("Sol");
		caisse = lisImage("Caisse");
		but = lisImage("But");
		caisseSurBut = lisImage("CaisseSurBut");

		pousseurs = new ImageSokoban[4][4];
		for (int d = 0; d < pousseurs.length; d++)
			for (int i = 0; i < pousseurs[0].length; i++)
				pousseurs[d][i] = lisImage("Pousseur_" + d + "_" + i);
		etape = 0;
		direction = 2;
		metAJourPousseur();
	}

	private ImageSokoban lisImage(String nom) {
		String ressource = Configuration.instance().lis(nom);
		Configuration.instance().logger().info("Lecture de l'image " + ressource + " comme " + nom);
		InputStream in = Configuration.charge(ressource);
		try {
			// Chargement d'une image utilisable dans Swing
			return super.lisImage(in);
		} catch (Exception e) {
			Configuration.instance().logger().severe("Impossible de charger l'image " + ressource);
			System.exit(1);
		}
		return null;
	}

	@Override
	int hauteurCase() {
		return hauteurCase;
	}

	@Override
	int largeurCase() {
		return largeurCase;
	}

	@Override
	public void decale(int l, int c, double dl, double dc) {
		if ((dl != 0) || (dc != 0)) {
			Vecteur v = decalages[l][c];
			if (v == null) {
				v = new Vecteur();
				decalages[l][c] = v;
			}
			v.x = dc;
			v.y = dl;
		} else {
			decalages[l][c] = null;
		}
		miseAJour();
	}

	@Override
	void tracerNiveau() {
		Niveau n = j.niveau();

		largeurCase = largeur() / n.colonnes();
		hauteurCase = hauteur() / n.lignes();
		// On prend des cases carrées
		largeurCase = Math.min(largeurCase, hauteurCase);
		hauteurCase = largeurCase;

		// Le vecteur de décalages doit être conforme au niveau
		if ((decalages == null) || (decalages.length != n.lignes()) || (decalages[0].length != n.colonnes()))
			decalages = new Vecteur[n.lignes()][n.colonnes()];

		// Tracé du niveau
		// En deux étapes à cause des décalages possibles
		for (int ligne = 0; ligne < n.lignes(); ligne++)
			for (int colonne = 0; colonne < n.colonnes(); colonne++) {
				int x = colonne * largeurCase;
				int y = ligne * hauteurCase;
				int marque = n.marque(ligne, colonne);
				// Tracé du sol
				if (n.aBut(ligne, colonne))
					tracer(but, x, y, largeurCase, hauteurCase);
				else
					tracer(sol, x, y, largeurCase, hauteurCase);
				if (marque > 0)
					tracerCroix(marque, x, y, largeurCase, hauteurCase);
			}
		for (int ligne = 0; ligne < n.lignes(); ligne++)
			for (int colonne = 0; colonne < n.colonnes(); colonne++) {
				int x = colonne * largeurCase;
				int y = ligne * hauteurCase;
				int marque = n.marque(ligne, colonne);
				Vecteur decal = decalages[ligne][colonne];
				if (decal != null) {
					x += decal.x * largeurCase;
					y += decal.y * hauteurCase;
				}
				// Tracé des objets, on enlève les tests exclusifs pour voir les bugs éventuels
				// de déplacement causés par l'IA
				if (n.aMur(ligne, colonne))
					tracer(mur, x, y, largeurCase, hauteurCase);
				if (n.aCaisse(ligne, colonne)) {
					if (n.aBut(ligne, colonne))
						tracer(caisseSurBut, x, y, largeurCase, hauteurCase);
					else
						tracer(caisse, x, y, largeurCase, hauteurCase);
					if (marque > 0)
						tracerCroix(marque, x, y, largeurCase, hauteurCase);
				}
				if (n.aPousseur(ligne, colonne))
					tracer(pousseur, x, y, largeurCase, hauteurCase);
			}

	}

	// Animation du pousseur
	void metAJourPousseur() {
		pousseur = pousseurs[direction][etape];
	}

	@Override
	public void metAJourDirection(int dL, int dC) {
		switch (dL + 2 * dC) {
			case -2:
				direction = 1;
				break;
			case -1:
				direction = 0;
				break;
			case 1:
				direction = 2;
				break;
			case 2:
				direction = 3;
				break;
			default:
				Configuration.instance().logger().severe("Bug interne, direction invalide");
		}
		metAJourPousseur();
	}

	@Override
	public void changeEtape() {
		etape = (etape + 1) % pousseurs[direction].length;
		metAJourPousseur();
		miseAJour();
	}
}