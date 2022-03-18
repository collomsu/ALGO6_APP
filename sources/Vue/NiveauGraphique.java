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

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import Global.Configuration;
import Modele.Jeu;
import Modele.Niveau;
import Patterns.Observateur;

import javax.imageio.ImageIO;
import javax.swing.*;

public abstract class NiveauGraphique extends JComponent implements Observateur {
	Graphics2D drawable;

	// La partie indépendante de Swing de la lecture des images se trouve dans le descendant
	protected ImageSokoban lisImage(InputStream in) throws IOException {
		return new ImageSokoban(ImageIO.read(in));
	}

	protected void tracer(ImageSokoban i, int x, int y, int l, int h) {
		drawable.drawImage(i.image(), x, y, l, h, null);
	}

	protected void tracerCroix(int marque, int x, int y, int l, int h) {
		int s = l/10;
		drawable.setColor(new Color(marque));
		drawable.setStroke(new BasicStroke(s));
		drawable.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawable.drawLine(x+s, y+s, x+l-s, y+h-s);
		drawable.drawLine(x+h-s, y+s, x+s, y+h-s);
	}

	// tracerNiveau est la partie indépendante de Swing du dessin qui se trouve dans le descendant
	abstract void tracerNiveau();

	public void paintComponent(Graphics g) {
		drawable = (Graphics2D) g;

		// On efface tout
		drawable.clearRect(0, 0, largeur(), hauteur());
		tracerNiveau();
	}

	int hauteur() {
		return getHeight();
	}

	int largeur() {
		return getWidth();
	}

	abstract int hauteurCase();

	abstract int largeurCase();

	@Override
	public void miseAJour() {
		repaint();
	}

	// Méthodes pour les animations
	// décale un des éléments d'une fraction de case (pour les animations)
	public abstract void decale(int l, int c, double dl, double dc);
    // Changements du pousseur
	public abstract void metAJourDirection(int dL, int dC);
	public abstract void changeEtape();
}