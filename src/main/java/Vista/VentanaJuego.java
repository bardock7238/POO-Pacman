package Vista;

import Controlador.JuegoControladorFX;
import Modelo.JuegoModelo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Ventana JavaFX del juego. Reemplaza completamente a VentanaPrincipal (Swing).
 * Aloja el Canvas y ensambla Modelo + Vista + Controlador en JavaFX puro.
 */
public class VentanaJuego {

    private Stage escenario;
    private JuegoControladorFX controlador;

    public VentanaJuego() {
        // Crear en el hilo JavaFX
        Platform.runLater(this::construir);
    }

    private void construir() {
        JuegoModelo modelo = new JuegoModelo();
        Canvas canvas = new Canvas(JuegoVista.ANCHO, JuegoVista.ALTO);

        VolumenHUD volumenHUD = new VolumenHUD();
        JuegoVista vista = new JuegoVista(modelo, volumenHUD);
        escenario = new Stage();
        controlador = new JuegoControladorFX(modelo, vista, canvas, escenario, volumenHUD);

        StackPane root = new StackPane(canvas);
        root.setBackground(new Background(new BackgroundFill(
            Color.web("#050510"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, JuegoVista.ANCHO, JuegoVista.ALTO);

        escenario.setTitle("Pac-Man — POO 2026");
        escenario.setScene(scene);
        escenario.setResizable(false);
        escenario.initStyle(StageStyle.DECORATED);
        escenario.setOnCloseRequest(e -> controlador.detener());
        escenario.show();

        // Dar foco al canvas para que reciba teclas
        canvas.requestFocus();
        controlador.iniciar();
    }
}
