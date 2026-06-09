package Vista;

import Modelo.*;
import Modelo.JuegoModelo.EstadoJuego;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.effect.Glow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.ArcType;

/**
 * Vista JavaFX pura del juego.
 * Renderiza todo sobre un Canvas usando GraphicsContext.
 * Sin ninguna dependencia de Swing/AWT.
 */
public class JuegoVista {

    // ── Colores ────────────────────────────────────────────────────────────────
    private static final Color COL_FONDO      = Color.web("#050510");
    private static final Color COL_PARED      = Color.web("#0a1aff");
    private static final Color COL_PARED_BRD  = Color.web("#4466ff");
    private static final Color COL_PARED_GLOW = Color.web("#2233cc");
    private static final Color COL_PELLET     = Color.web("#ffddbb");
    private static final Color COL_POWERUP    = Color.web("#ffaa00");
    private static final Color COL_HUD        = Color.web("#ffdd00");
    private static final Color COL_PACMAN     = Color.web("#ffe000");
    private static final Color COL_PACMAN_EYE = Color.web("#000000");

    // ── Tamaños ────────────────────────────────────────────────────────────────
    private static final int TAM  = Laberinto.TAM_CELDA;
    private static final int COLS = Laberinto.COLUMNAS;
    private static final int FILA = Laberinto.FILAS;
    public  static final int ANCHO = COLS * TAM;
    public  static final int ALTO  = FILA * TAM + 56;

    private JuegoModelo modelo;
    private long frameCount = 0;
    private VolumenHUD volumenHUD;

    public JuegoVista(JuegoModelo modelo, VolumenHUD volumenHUD) {
        this.modelo     = modelo;
        this.volumenHUD = volumenHUD;
    }

    /** Llamado cada frame desde el AnimationTimer. */
    public void render(GraphicsContext g) {
        frameCount++;

        // Fondo
        g.setFill(COL_FONDO);
        g.fillRect(0, 0, ANCHO, ALTO);

        // Líneas de fondo sutil (efecto cuadrícula)
        g.setStroke(Color.web("#0a0a2a"));
        g.setLineWidth(0.5);
        for (int c = 0; c < COLS; c++) {
            g.strokeLine(c * TAM, 0, c * TAM, FILA * TAM);
        }
        for (int f = 0; f < FILA; f++) {
            g.strokeLine(0, f * TAM, ANCHO, f * TAM);
        }

        dibujarLaberinto(g);
        dibujarPacMan(g);
        dibujarFantasmas(g);
        dibujarHUD(g);
        dibujarMensajeEstado(g);
        volumenHUD.render(g, ANCHO);
    }

    // ── Laberinto ─────────────────────────────────────────────────────────────

    private void dibujarLaberinto(GraphicsContext g) {
        int[][] mapa = modelo.getLaberinto().getMapa();

        for (int f = 0; f < FILA; f++) {
            for (int c = 0; c < COLS; c++) {
                int px = c * TAM;
                int py = f * TAM;
                int celda = mapa[f][c];

                if (celda == 1) {
                    dibujarPared(g, px, py);
                } else if (celda == 0) {
                    dibujarPellet(g, px, py);
                } else if (celda == 3) {
                    dibujarPowerUp(g, px, py);
                }
            }
        }
    }

    private void dibujarPared(GraphicsContext g, int px, int py) {
        // Relleno con gradiente azul oscuro
        LinearGradient grad = new LinearGradient(
            px, py, px + TAM, py + TAM, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0a18cc")),
            new Stop(1, Color.web("#06108a"))
        );
        g.setFill(grad);
        g.fillRoundRect(px + 1, py + 1, TAM - 2, TAM - 2, 7, 7);

        // Borde brillante
        g.setStroke(COL_PARED_BRD);
        g.setLineWidth(1.2);
        g.strokeRoundRect(px + 1, py + 1, TAM - 2, TAM - 2, 7, 7);

        // Reflejo superior para efecto 3D
        g.setStroke(Color.web("#6688ff", 0.35));
        g.setLineWidth(1.0);
        g.strokeLine(px + 3, py + 2, px + TAM - 4, py + 2);
    }

    private void dibujarPellet(GraphicsContext g, int px, int py) {
        double cx = px + TAM / 2.0;
        double cy = py + TAM / 2.0;
        double r = 2.8;
        // Halo suave
        g.setFill(Color.web("#ffddbb", 0.25));
        g.fillOval(cx - r * 2, cy - r * 2, r * 4, r * 4);
        // Pellet
        g.setFill(COL_PELLET);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void dibujarPowerUp(GraphicsContext g, int px, int py) {
        double cx = px + TAM / 2.0;
        double cy = py + TAM / 2.0;
        // Pulso: radio varía con el frame
        double pulso = 5.5 + Math.sin(frameCount * 0.12) * 1.8;
        // Halo
        RadialGradient glow = new RadialGradient(0, 0, cx, cy, pulso * 2.2,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#ffaa00", 0.55)),
            new Stop(1, Color.web("#ffaa00", 0))
        );
        g.setFill(glow);
        g.fillOval(cx - pulso * 2.2, cy - pulso * 2.2, pulso * 4.4, pulso * 4.4);
        // Cuerpo
        g.setFill(COL_POWERUP);
        g.fillOval(cx - pulso, cy - pulso, pulso * 2, pulso * 2);
        // Brillo
        g.setFill(Color.web("#ffffff", 0.5));
        g.fillOval(cx - pulso * 0.5, cy - pulso * 0.7, pulso * 0.55, pulso * 0.45);
    }

    // ── Pac-Man ───────────────────────────────────────────────────────────────

    private void dibujarPacMan(GraphicsContext g) {
        PacMan p = modelo.getPacman();

        // Parpadeo cuando es invulnerable
        if (p.isInvulnerable() && (System.currentTimeMillis() / 120) % 2 == 0) return;

        double angulo = p.getAnguloBoca();
        double inicio = getAnguloInicio(p.getDireccion(), angulo);
        double barrido = 360 - angulo * 2;

        double px = p.getX();
        double py = p.getY();

        // Halo amarillo suave
        RadialGradient halo = new RadialGradient(0, 0,
            px + TAM / 2.0, py + TAM / 2.0, TAM * 0.72,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#ffe000", 0.38)),
            new Stop(1, Color.web("#ffe000", 0))
        );
        g.setFill(halo);
        g.fillOval(px - TAM * 0.22, py - TAM * 0.22, TAM * 1.44, TAM * 1.44);

        // Cuerpo con gradiente radial
        RadialGradient cuerpo = new RadialGradient(0, 0,
            px + TAM * 0.38, py + TAM * 0.32, TAM * 0.62,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#fff176")),
            new Stop(0.65, COL_PACMAN),
            new Stop(1, Color.web("#cc9900"))
        );
        g.setFill(cuerpo);
        g.fillArc(px, py, TAM, TAM, inicio, barrido, ArcType.ROUND);

        // Ojo (posición según dirección)
        double[] ojo = getOjoPos(p.getDireccion(), px, py);
        g.setFill(COL_PACMAN_EYE);
        g.fillOval(ojo[0], ojo[1], 3.5, 3.5);
    }

    private double getAnguloInicio(PacMan.Direccion dir, double angulo) {
        switch (dir) {
            case ARRIBA:    return 90  + angulo;
            case ABAJO:     return 270 + angulo;
            case IZQUIERDA: return 180 + angulo;
            default:        return angulo;
        }
    }

    private double[] getOjoPos(PacMan.Direccion dir, double px, double py) {
        switch (dir) {
            case ARRIBA:    return new double[]{px + TAM * 0.6, py + TAM * 0.25};
            case ABAJO:     return new double[]{px + TAM * 0.35, py + TAM * 0.25};
            case IZQUIERDA: return new double[]{px + TAM * 0.3,  py + TAM * 0.22};
            default:        return new double[]{px + TAM * 0.55, py + TAM * 0.22};
        }
    }

    // ── Fantasmas ─────────────────────────────────────────────────────────────

    private void dibujarFantasmas(GraphicsContext g) {
        for (Fantasma f : modelo.getFantasmas()) {
            if (!f.isActivo()) continue;
            dibujarFantasma(g, f);
        }
    }

    private void dibujarFantasma(GraphicsContext g, Fantasma f) {
        boolean asustado = f.getEstado() == Fantasma.EstadoFantasma.ASUSTADO;
        java.awt.Color awtColor = f.getColor();
        Color color = asustado
            ? Color.web("#1133dd")
            : Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

        double px = f.getX();
        double py = f.getY();
        double w = TAM;
        double h = TAM;

        // Halo del color del fantasma
        if (!asustado) {
            g.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.22));
            g.fillOval(px - 2, py - 2, w + 4, h + 4);
        }

        // Gradiente del cuerpo
        RadialGradient cuerpo = new RadialGradient(0, 0,
            px + w * 0.38, py + h * 0.28, w * 0.65,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, color.brighter()),
            new Stop(1, color.darker())
        );
        g.setFill(cuerpo);

        // Cabeza semicircular
        g.fillArc(px, py, w, h, 0, 180, ArcType.CHORD);

        // Cuerpo rectangular
        g.fillRect(px, py + h / 2, w, h / 2 - 3);

        // Faldón ondulado (3 picos)
        double base = py + h - 1;
        double pit  = base - 5;
        double[] xs = { px, px + w/6, px + w*2/6, px + w*3/6, px + w*4/6, px + w*5/6, px + w };
        double[] ys = { base, pit, base, pit, base, pit, base };
        g.fillPolygon(xs, ys, 7);

        // Ojos
        if (!asustado) {
            // Esclerótica
            g.setFill(Color.WHITE);
            g.fillOval(px + w * 0.17, py + h * 0.20, w * 0.28, h * 0.30);
            g.fillOval(px + w * 0.55, py + h * 0.20, w * 0.28, h * 0.30);
            // Pupila
            g.setFill(Color.BLUE);
            g.fillOval(px + w * 0.20, py + h * 0.28, w * 0.16, h * 0.18);
            g.fillOval(px + w * 0.58, py + h * 0.28, w * 0.16, h * 0.18);
            // Brillo
            g.setFill(Color.WHITE);
            g.fillOval(px + w * 0.24, py + h * 0.28, w * 0.07, h * 0.07);
            g.fillOval(px + w * 0.62, py + h * 0.28, w * 0.07, h * 0.07);
        } else {
            // Cara asustada con dientes temblorosos
            g.setStroke(Color.WHITE);
            g.setLineWidth(1.5);
            double bx = px + w * 0.2;
            double by = py + h * 0.58;
            double seg = w * 0.6 / 4;
            for (int i = 0; i < 4; i++) {
                g.strokeLine(bx + i * seg, by + (i % 2 == 0 ? 0 : -4),
                             bx + (i + 1) * seg, by + (i % 2 == 0 ? -4 : 0));
            }
            // Ojos X
            g.setLineWidth(1.4);
            g.strokeLine(px + w*0.20, py + h*0.22, px + w*0.34, py + h*0.36);
            g.strokeLine(px + w*0.34, py + h*0.22, px + w*0.20, py + h*0.36);
            g.strokeLine(px + w*0.58, py + h*0.22, px + w*0.72, py + h*0.36);
            g.strokeLine(px + w*0.72, py + h*0.22, px + w*0.58, py + h*0.36);
        }
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void dibujarHUD(GraphicsContext g) {
        double yBase = FILA * TAM;

        // Fondo del HUD
        LinearGradient hudFondo = new LinearGradient(0, yBase, 0, yBase + 56,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#05051a")),
            new Stop(1, Color.web("#000008"))
        );
        g.setFill(hudFondo);
        g.fillRect(0, yBase, ANCHO, 56);

        // Línea separadora
        g.setStroke(COL_PARED_BRD);
        g.setLineWidth(1.5);
        g.strokeLine(0, yBase, ANCHO, yBase);

        // Texto puntos
        g.setFill(COL_HUD);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        g.setTextAlign(TextAlignment.LEFT);
        g.fillText("PUNTOS", 12, yBase + 18);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        g.fillText(String.valueOf(modelo.getPuntuacion()), 12, yBase + 44);

        // Pellets restantes
        int restantes = modelo.getLaberinto().getPelletsRestantes();
        int total     = modelo.getLaberinto().getTotalPellets();
        double pct    = total > 0 ? (double)(total - restantes) / total : 0;

        g.setFill(Color.web("#333355"));
        g.fillRoundRect(ANCHO / 2.0 - 50, yBase + 12, 100, 8, 5, 5);
        g.setFill(Color.web("#44ffaa"));
        g.fillRoundRect(ANCHO / 2.0 - 50, yBase + 12, 100 * pct, 8, 5, 5);
        g.setFill(Color.web("#aaaacc"));
        g.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText(restantes + " pellets", ANCHO / 2.0, yBase + 34);

        // Vidas como mini pac-men
        g.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        g.setFill(COL_HUD);
        g.setTextAlign(TextAlignment.RIGHT);
        g.fillText("VIDAS", ANCHO - 12, yBase + 18);
        for (int i = 0; i < modelo.getVidas(); i++) {
            double vx = ANCHO - 18 - i * 22;
            double vy = yBase + 24;
            g.setFill(COL_PACMAN);
            g.fillArc(vx, vy, 16, 16, 30, 300, ArcType.ROUND);
        }
    }

    // ── Mensajes de estado ────────────────────────────────────────────────────

    private void dibujarMensajeEstado(GraphicsContext g) {
        EstadoJuego estado = modelo.getEstadoJuego();
        if (estado == EstadoJuego.EN_CURSO) return;

        String titulo = null;
        String subtitulo = null;
        Color colTitulo = Color.WHITE;

        switch (estado) {
            case INICIO:
                titulo    = "PAC-MAN";
                subtitulo = "Presiona  ENTER  para jugar";
                colTitulo = Color.web("#ffff00");
                break;
            case PAUSADO:
                titulo    = "PAUSA";
                subtitulo = "ENTER: continuar   R: reiniciar   ESC: menú";
                colTitulo = Color.web("#ffaa00");
                break;
            case VICTORIA:
                titulo    = "¡GANASTE!";
                subtitulo = "Presiona  R  para reiniciar";
                colTitulo = Color.web("#44ff88");
                break;
            case GAME_OVER:
                titulo    = "GAME OVER";
                subtitulo = "Presiona  R  para reiniciar";
                colTitulo = Color.web("#ff3333");
                break;
            default:
                break;
        }

        if (titulo == null) return;

        double w = ANCHO;
        double h = FILA * TAM;
        double cy = h / 2.0;

        // Overlay semitransparente con gradiente
        LinearGradient overlay = new LinearGradient(0, cy - 55, 0, cy + 55,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#000000", 0)),
            new Stop(0.25, Color.web("#000000", 0.82)),
            new Stop(0.75, Color.web("#000000", 0.82)),
            new Stop(1, Color.web("#000000", 0))
        );
        g.setFill(overlay);
        g.fillRect(0, cy - 55, w, 110);

        // Borde decorativo
        g.setStroke(colTitulo);
        g.setLineWidth(1.5);
        g.strokeLine(w * 0.1, cy - 50, w * 0.9, cy - 50);
        g.strokeLine(w * 0.1, cy + 50, w * 0.9, cy + 50);

        // Pulso del título
        double escala = 1.0 + Math.sin(frameCount * 0.08) * 0.035;

        // Sombra del título
        g.setFill(colTitulo.darker().darker());
        g.setFont(Font.font("Arial", FontWeight.BOLD, 32 * escala));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText(titulo, w / 2 + 2, cy + 2);

        // Título principal
        g.setFill(colTitulo);
        g.fillText(titulo, w / 2, cy);

        // Subtítulo parpadeante
        boolean visible = (frameCount / 28) % 2 == 0;
        if (visible || estado == EstadoJuego.PAUSADO) {
            g.setFill(Color.web("#ccccff"));
            g.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            g.fillText(subtitulo, w / 2, cy + 28);
        }
    }
}
