package Vista;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;

import Controlador.JuegoControlador;
import Modelo.JuegoModelo;

import javax.swing.SwingUtilities;

public class MenuPrincipal extends Application {

    private double[] pelletX = new double[8];
    private double xPacman = -40;
    private double anguloBoca = 30;
    private boolean bocaAbriendo = false;
    private long tiempoAnterior = 0;

    @Override
    public void start(Stage escenario) {
        Canvas lienzo = new Canvas(456, 550);
        GraphicsContext gc = lienzo.getGraphicsContext2D();

        for (int i = 0; i < pelletX.length; i++) {
            pelletX[i] = 60 + i * 52;
        }

        // ── Título ─────────────────────────────────────────────────────
        Text titulo = new Text("PAC-MAN");
        titulo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 64));
        titulo.setFill(Color.YELLOW);
        DropShadow sombra = new DropShadow(20, Color.ORANGE);
        Glow brillo = new Glow(0.8);
        brillo.setInput(sombra);
        titulo.setEffect(brillo);

        Text subtitulo = new Text("POO 2026 — Universidad Distrital");
        subtitulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        subtitulo.setFill(Color.LIGHTCYAN);

        // ── Botones ────────────────────────────────────────────────────
        Button btnJugar         = crearBoton("▶  JUGAR",         "#FFD700", "#FF8C00");
        Button btnInstrucciones = crearBoton("?  INSTRUCCIONES", "#00FFFF", "#0088AA");
        Button btnSalir         = crearBoton("✕  SALIR",         "#FF4444", "#AA0000");

        btnJugar.setOnAction(e -> iniciarJuego(escenario));
        btnInstrucciones.setOnAction(e -> mostrarInstrucciones(escenario));
        btnSalir.setOnAction(e -> { escenario.close(); Platform.exit(); System.exit(0); });

        VBox contenedor = new VBox(18, titulo, subtitulo, btnJugar, btnInstrucciones, btnSalir);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setTranslateY(-30);

        StackPane raiz = new StackPane(lienzo, contenedor);
        raiz.setStyle("-fx-background-color: black;");

        Scene escena = new Scene(raiz, 456, 550);
        escena.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) iniciarJuego(escenario); });

        // ── Animación fondo ────────────────────────────────────────────
        new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                if (tiempoAnterior == 0) { tiempoAnterior = ahora; return; }
                double dt = (ahora - tiempoAnterior) / 1_000_000_000.0;
                tiempoAnterior = ahora;

                gc.clearRect(0, 0, 456, 550);

                LinearGradient degradado = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#000010")), new Stop(1, Color.web("#000030")));
                gc.setFill(degradado);
                gc.fillRect(0, 0, 456, 550);

                xPacman += 120 * dt;
                if (xPacman > 500) xPacman = -40;

                if (bocaAbriendo) { anguloBoca += 120 * dt; if (anguloBoca >= 40) bocaAbriendo = false; }
                else              { anguloBoca -= 120 * dt; if (anguloBoca <= 3)  bocaAbriendo = true;  }

                gc.setFill(Color.YELLOW);
                gc.fillArc(xPacman, 400, 32, 32, anguloBoca, 360 - anguloBoca * 2,
                           javafx.scene.shape.ArcType.ROUND);

                gc.setFill(Color.web("#FFE0B0"));
                for (double px : pelletX) {
                    if (px > xPacman + 16) gc.fillOval(px, 412, 8, 8);
                }

                gc.setStroke(Color.web("#0033AA"));
                gc.setLineWidth(2);
                gc.strokeLine(0, 395, 456, 395);
            }
        }.start();

        escenario.setTitle("Pac-Man — Menú Principal");
        escenario.setScene(escena);
        escenario.setResizable(false);
        escenario.show();
    }

    private Button crearBoton(String texto, String colorTexto, String colorSombra) {
        Button boton = new Button(texto);
        boton.setPrefWidth(240);
        boton.setPrefHeight(48);
        boton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        String estiloNormal = "-fx-background-color: #111122; -fx-text-fill: " + colorTexto +
            "; -fx-border-color: " + colorTexto + "; -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        String estiloHover = "-fx-background-color: #222244; -fx-text-fill: " + colorTexto +
            "; -fx-border-color: " + colorTexto + "; -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        boton.setStyle(estiloNormal);
        boton.setEffect(new DropShadow(10, Color.web(colorSombra)));
        boton.setOnMouseEntered(e -> boton.setStyle(estiloHover));
        boton.setOnMouseExited(e -> boton.setStyle(estiloNormal));
        return boton;
    }

    private void iniciarJuego(Stage escenario) {
        escenario.close();
        Platform.exit();
        SwingUtilities.invokeLater(() -> {
            JuegoModelo modelo = new JuegoModelo();
            VentanaPrincipal vista = new VentanaPrincipal(modelo);
            new JuegoControlador(modelo, vista);
            vista.getJuegoPanel().requestFocusInWindow();
        });
    }

    private void mostrarInstrucciones(Stage duenio) {
        Stage ventana = new Stage();
        ventana.initOwner(duenio);
        ventana.setTitle("Instrucciones");

        Text texto = new Text(
            "  CONTROLES\n\n" +
            "  ↑ W   — Arriba\n" +
            "  ↓ S   — Abajo\n" +
            "  ← A   — Izquierda\n" +
            "  → D   — Derecha\n\n" +
            "  ENTER — Iniciar / Reanudar\n" +
            "  P     — Pausar\n" +
            "  R     — Reiniciar\n\n" +
            "  OBJETIVO\n\n" +
            "  Come todos los pellets sin que\n" +
            "  los fantasmas te atrapen.\n" +
            "  Los pellets grandes ( ● ) asustan\n" +
            "  a los fantasmas — ¡cómetelos!"
        );
        texto.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        texto.setFill(Color.LIGHTCYAN);

        VBox caja = new VBox(texto);
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.setStyle("-fx-background-color: #000020; -fx-padding: 30;");

        Button btnCerrar = crearBoton("CERRAR", "#FFD700", "#FF8C00");
        btnCerrar.setOnAction(e -> ventana.close());

        VBox raiz = new VBox(20, caja, btnCerrar);
        raiz.setAlignment(Pos.CENTER);
        raiz.setStyle("-fx-background-color: #000020;");

        ventana.setScene(new Scene(raiz, 320, 420));
        ventana.setResizable(false);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}