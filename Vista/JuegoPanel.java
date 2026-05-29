package Vista;

import Modelo.*;
import Modelo.JuegoModelo.EstadoJuego;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.List;

/**
 * Panel principal de renderizado (MVC - Vista).
 * Dibuja el laberinto, PacMan, fantasmas y el HUD.
 */
public class JuegoPanel extends JPanel {

    private JuegoModelo modelo;

    private static final Color COLOR_PARED     = new Color(0, 0, 180);
    private static final Color COLOR_PARED_BRD = new Color(0, 100, 255);
    private static final Color COLOR_PELLET    = new Color(255, 220, 180);
    private static final Color COLOR_POWERUP   = new Color(255, 180, 80);
    private static final Color COLOR_FONDO     = Color.BLACK;
    private static final Color COLOR_HUD_TEXT  = new Color(255, 220, 0);

    public JuegoPanel(JuegoModelo modelo) {
        this.modelo = modelo;
        int ancho = Laberinto.COLUMNAS * Laberinto.TAM_CELDA;
        int alto  = Laberinto.FILAS    * Laberinto.TAM_CELDA + 50;
        setPreferredSize(new Dimension(ancho, alto));
        setBackground(COLOR_FONDO);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dibujarLaberinto(g2);
        dibujarPacMan(g2);
        dibujarFantasmas(g2);
        dibujarHUD(g2);
        dibujarMensajeEstado(g2);
    }

    // ─── Laberinto ──────────────────────────────────────────────────────────────

    private void dibujarLaberinto(Graphics2D g) {
        int[][] mapa = modelo.getLaberinto().getMapa();
        int tam = Laberinto.TAM_CELDA;

        for (int f = 0; f < Laberinto.FILAS; f++) {
            for (int c = 0; c < Laberinto.COLUMNAS; c++) {
                int px = c * tam;
                int py = f * tam;
                int celda = mapa[f][c];

                if (celda == 1) {
                    // Pared con efecto de borde
                    g.setColor(COLOR_PARED);
                    g.fillRoundRect(px + 1, py + 1, tam - 2, tam - 2, 6, 6);
                    g.setColor(COLOR_PARED_BRD);
                    g.setStroke(new BasicStroke(1.2f));
                    g.drawRoundRect(px + 1, py + 1, tam - 2, tam - 2, 6, 6);
                } else if (celda == 0) {
                    // Pellet normal
                    g.setColor(COLOR_PELLET);
                    int r = 3;
                    g.fillOval(px + tam/2 - r, py + tam/2 - r, r*2, r*2);
                } else if (celda == 3) {
                    // Power-up — pellet grande pulsante
                    g.setColor(COLOR_POWERUP);
                    int r = 6;
                    g.fillOval(px + tam/2 - r, py + tam/2 - r, r*2, r*2);
                }
            }
        }
    }

    // ─── Pac-Man ────────────────────────────────────────────────────────────────

    private void dibujarPacMan(Graphics2D g) {
        PacMan p = modelo.getPacman();
        int tam = Laberinto.TAM_CELDA;
        int angulo = p.getAnguloBoca();

        // Parpadeo cuando es invulnerable
        if (p.isInvulnerable() && (System.currentTimeMillis() / 150) % 2 == 0) return;

        int startAngle = getAnguloInicio(p.getDireccion(), angulo);

        g.setColor(Color.YELLOW);
        Arc2D arco = new Arc2D.Float(p.getX(), p.getY(), tam, tam,
                                     startAngle, 360 - angulo * 2, Arc2D.PIE);
        g.fill(arco);

        // Ojo
        g.setColor(Color.BLACK);
        g.fillOval(p.getX() + tam/2 - 2, p.getY() + 4, 4, 4);
    }

    private int getAnguloInicio(PacMan.Direccion dir, int angulo) {
        switch (dir) {
            case ARRIBA:    return 90  + angulo;
            case ABAJO:     return 270 + angulo;
            case IZQUIERDA: return 180 + angulo;
            default:        return angulo; // Derecha o sin dirección
        }
    }

    // ─── Fantasmas ──────────────────────────────────────────────────────────────

    private void dibujarFantasmas(Graphics2D g) {
        int tam = Laberinto.TAM_CELDA;
        for (Fantasma f : modelo.getFantasmas()) {
            Color color = f.getEstado() == Fantasma.EstadoFantasma.ASUSTADO
                ? new Color(0, 0, 200) : f.getColor();
            int px = f.getX();
            int py = f.getY();

            // Cuerpo semicircular
            g.setColor(color);
            g.fillArc(px, py, tam, tam, 0, 180);
            g.fillRect(px, py + tam/2, tam, tam/2);

            // Faldón ondulado
            int[] xPts = {px, px+4, px+8, px+12, px+16, px+tam};
            int[] yPts = {py+tam, py+tam-4, py+tam, py+tam-4, py+tam, py+tam};
            g.fillPolygon(xPts, yPts, 6);

            // Ojos
            if (f.getEstado() != Fantasma.EstadoFantasma.ASUSTADO) {
                g.setColor(Color.WHITE);
                g.fillOval(px + 4,  py + 6, 6, 7);
                g.fillOval(px + 13, py + 6, 6, 7);
                g.setColor(Color.BLUE);
                g.fillOval(px + 5,  py + 8, 4, 4);
                g.fillOval(px + 14, py + 8, 4, 4);
            } else {
                // Cara asustada
                g.setColor(Color.WHITE);
                g.drawLine(px+4, py+14, px+8, py+10);
                g.drawLine(px+8, py+10, px+12, py+14);
                g.drawLine(px+12,py+14, px+16, py+10);
            }
        }
    }

    // ─── HUD ────────────────────────────────────────────────────────────────────

    private void dibujarHUD(Graphics2D g) {
        int yHUD = Laberinto.FILAS * Laberinto.TAM_CELDA + 10;
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(COLOR_HUD_TEXT);
        g.drawString("PUNTOS: " + modelo.getPuntuacion(), 10, yHUD + 20);

        // Vidas como ícono Pac-Man
        g.drawString("VIDAS:", 200, yHUD + 20);
        for (int i = 0; i < modelo.getVidas(); i++) {
            g.setColor(Color.YELLOW);
            g.fillArc(270 + i * 22, yHUD + 5, 16, 16, 30, 300);
        }
    }

    // ─── Mensajes de estado ─────────────────────────────────────────────────────

    private void dibujarMensajeEstado(Graphics2D g) {
        EstadoJuego estado = modelo.getEstadoJuego();
        String msg = null;
        Color color = Color.WHITE;

        switch (estado) {
            case INICIO:
                msg = "Presiona ENTER para jugar";
                color = Color.CYAN;
                break;
            case PAUSADO:
                msg = "PAUSA  —  ENTER para continuar";
                color = Color.ORANGE;
                break;
            case VICTORIA:
                msg = "¡GANASTE!  Presiona R para reiniciar";
                color = new Color(0, 255, 100);
                break;
            case GAME_OVER:
                msg = "GAME OVER  —  Presiona R para reiniciar";
                color = Color.RED;
                break;
            default:
                break;
        }

        if (msg != null) {
            int ancho = Laberinto.COLUMNAS * Laberinto.TAM_CELDA;
            int alto  = Laberinto.FILAS    * Laberinto.TAM_CELDA;

            // Fondo semitransparente
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(0, alto/2 - 28, ancho, 46);

            g.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            int tx = (ancho - fm.stringWidth(msg)) / 2;

            g.setColor(color.darker());
            g.drawString(msg, tx + 2, alto/2 + 10);
            g.setColor(color);
            g.drawString(msg, tx, alto/2 + 8);
        }
    }
}
