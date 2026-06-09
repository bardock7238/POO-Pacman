package Vista;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * HUD de volumen — barra flotante que aparece/desaparece con la tecla V.
 * Se dibuja encima del canvas del juego.
 */
public class VolumenHUD {

    private boolean visible = false;
    private long    tiempoVisible = 0;          // ms en que se mostró
    private static final long AUTO_OCULTAR_MS = 2500; // se oculta solo

    private static final double W_BARRA = 180;
    private static final double H_BARRA = 48;

    public boolean isVisible() { return visible; }

    /** Muestra el HUD y reinicia el temporizador de auto-ocultar. */
    public void mostrar() {
        visible = true;
        tiempoVisible = System.currentTimeMillis();
    }

    public void ocultar() { visible = false; }

    /** Sube el volumen 10% y muestra el HUD. */
    public void subir() {
        SonidoPacman.setVolumen(SonidoPacman.getVolumen() + 0.10);
        mostrar();
    }

    /** Baja el volumen 10% y muestra el HUD. */
    public void bajar() {
        SonidoPacman.setVolumen(SonidoPacman.getVolumen() - 0.10);
        mostrar();
    }

    /**
     * Dibuja la barra de volumen centrada en la parte superior del canvas.
     * Llama esto cada frame desde JuegoVista (solo pinta si visible==true).
     */
    public void render(GraphicsContext g, double canvasW) {
        // Auto-ocultar tras el tiempo definido
        if (visible && System.currentTimeMillis() - tiempoVisible > AUTO_OCULTAR_MS) {
            visible = false;
        }
        if (!visible) return;

        double x = (canvasW - W_BARRA) / 2.0;
        double y = 14;

        // Fondo con bordes redondeados
        g.setFill(Color.web("#000022", 0.88));
        g.fillRoundRect(x - 4, y - 4, W_BARRA + 8, H_BARRA + 8, 12, 12);
        g.setStroke(Color.web("#4466ff", 0.7));
        g.setLineWidth(1.5);
        g.strokeRoundRect(x - 4, y - 4, W_BARRA + 8, H_BARRA + 8, 12, 12);

        // Etiqueta
        g.setFill(Color.web("#aabbff"));
        g.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText("🔊  VOLUMEN  (↑ / ↓)", canvasW / 2.0, y + 13);

        // Carril de la barra
        double barY  = y + 22;
        double barH  = 10;
        g.setFill(Color.web("#1a1a44"));
        g.fillRoundRect(x, barY, W_BARRA, barH, 5, 5);

        // Relleno según nivel
        double vol  = SonidoPacman.getVolumen();
        double fill = W_BARRA * vol;
        Color colIzq = vol > 0.66 ? Color.web("#ff4444")
                     : vol > 0.33 ? Color.web("#ffaa00")
                                  : Color.web("#44ff88");
        LinearGradient grad = new LinearGradient(x, 0, x + fill, 0,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, colIzq.darker()),
            new Stop(1, colIzq)
        );
        if (fill > 1) {
            g.setFill(grad);
            g.fillRoundRect(x, barY, fill, barH, 5, 5);
        }

        // Porcentaje
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        g.fillText((int)(vol * 100) + "%", canvasW / 2.0, y + 46);
    }
}
