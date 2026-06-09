package Vista;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Menú principal del juego — JavaFX puro.
 * Lanza VentanaJuego (JavaFX) en lugar de Swing al pulsar Jugar.
 */
public class MenuPrincipal extends Application {

    private static Stage stageGlobal;

    private double[] pelletX = new double[8];
    private double xPacman = -40;
    private double anguloBoca = 30;
    private boolean bocaAbriendo = false;
    private long tiempoAnterior = 0;

    private static final int W = 456;
    private static final int H = 580;
    private static final int H_PACMAN_ZONE = 80;

    @Override
    public void start(Stage escenario) {
        Platform.setImplicitExit(false);
        stageGlobal = escenario;

        Canvas lienzo = new Canvas(W, H + H_PACMAN_ZONE);
        GraphicsContext gc = lienzo.getGraphicsContext2D();

        for (int i = 0; i < pelletX.length; i++) {
            pelletX[i] = 60 + i * 52;
        }

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

        Button btnJugar         = crearBoton("▶  JUGAR",         "#FFD700", "#FF8C00");
        Button btnInstrucciones = crearBoton("?  INSTRUCCIONES", "#00FFFF", "#0088AA");
        Button btnTablero       = crearBoton("🏆  TABLERO",      "#AAFFAA", "#007700");
        Button btnSalir         = crearBoton("✕  SALIR",         "#FF4444", "#AA0000");

        btnJugar.setOnAction(e -> iniciarJuego(escenario));
        btnInstrucciones.setOnAction(e -> mostrarInstrucciones(escenario));
        btnTablero.setOnAction(e -> mostrarTablero(escenario));
        btnSalir.setOnAction(e -> { escenario.close(); System.exit(0); });

        VBox contenedor = new VBox(14, titulo, subtitulo,
                btnJugar, btnInstrucciones, btnTablero, btnSalir);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefSize(W, H);

        StackPane raiz = new StackPane(lienzo, contenedor);
        raiz.setStyle("-fx-background-color: black;");
        raiz.setPrefSize(W, H + H_PACMAN_ZONE);
        StackPane.setAlignment(contenedor, Pos.TOP_CENTER);

        Scene escena = new Scene(raiz, W, H + H_PACMAN_ZONE);
        escena.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) iniciarJuego(escenario); });

        new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                if (tiempoAnterior == 0) { tiempoAnterior = ahora; return; }
                double dt = (ahora - tiempoAnterior) / 1_000_000_000.0;
                tiempoAnterior = ahora;

                int total = H + H_PACMAN_ZONE;
                gc.clearRect(0, 0, W, total);

                LinearGradient degradado = new LinearGradient(0, 0, 0, 1, true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#000010")),
                        new Stop(1, Color.web("#000030")));
                gc.setFill(degradado);
                gc.fillRect(0, 0, W, total);

                gc.setStroke(Color.web("#0033AA"));
                gc.setLineWidth(2);
                gc.strokeLine(0, H, W, H);

                xPacman += 120 * dt;
                if (xPacman > W + 20) xPacman = -40;

                if (bocaAbriendo) { anguloBoca += 120 * dt; if (anguloBoca >= 40) bocaAbriendo = false; }
                else              { anguloBoca -= 120 * dt; if (anguloBoca <= 3)  bocaAbriendo = true;  }

                double pacY = H + (H_PACMAN_ZONE - 32) / 2.0;
                gc.setFill(Color.YELLOW);
                gc.fillArc(xPacman, pacY, 32, 32, anguloBoca, 360 - anguloBoca * 2, ArcType.ROUND);

                gc.setFill(Color.web("#FFE0B0"));
                double pelletY = H + H_PACMAN_ZONE / 2.0 - 4;
                for (double px : pelletX) {
                    if (px > xPacman + 16) gc.fillOval(px, pelletY, 8, 8);
                }
            }
        }.start();

        escenario.setTitle("Pac-Man — Menú Principal");
        escenario.setScene(escena);
        escenario.setResizable(false);
        escenario.show();
    }

    public static void mostrarMenu() {
        if (stageGlobal != null) {
            Platform.runLater(() -> stageGlobal.show());
        }
    }

    /**
     * Lanza el juego completamente en JavaFX — sin Swing.
     */
    private void iniciarJuego(Stage escenario) {
        escenario.hide();
        new VentanaJuego(); // JavaFX puro
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

    private void mostrarTablero(Stage duenio) {
        Stage ventana = new Stage();
        ventana.initOwner(duenio);
        ventana.setTitle("🏆 Tablero de Puntuación");

        Text tituloT = new Text("🏆  TOP 5 PUNTAJES");
        tituloT.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        tituloT.setFill(Color.YELLOW);

        java.util.List<String> lineas = Tablero.cargarPuntajes();
        VBox lista = new VBox(10);
        lista.setAlignment(Pos.CENTER_LEFT);

        if (lineas.isEmpty()) {
            Text vacio = new Text("  Aún no hay puntajes guardados.\n  ¡Juega una partida primero!");
            vacio.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
            vacio.setFill(Color.LIGHTGRAY);
            lista.getChildren().add(vacio);
        } else {
            String[] medallas = {"🥇", "🥈", "🥉", "4.", "5."};
            for (int i = 0; i < lineas.size(); i++) {
                Text entry = new Text("  " + medallas[i] + "  " + lineas.get(i));
                entry.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
                entry.setFill(i == 0 ? Color.GOLD : i == 1 ? Color.SILVER : Color.web("#cd7f32"));
                if (i > 2) entry.setFill(Color.LIGHTCYAN);
                lista.getChildren().add(entry);
            }
        }

        Button btnCerrar = crearBoton("CERRAR", "#FFD700", "#FF8C00");
        btnCerrar.setOnAction(e -> ventana.close());

        VBox raiz = new VBox(20, tituloT, lista, btnCerrar);
        raiz.setAlignment(Pos.CENTER);
        raiz.setStyle("-fx-background-color: #000020; -fx-padding: 30;");
        ventana.setScene(new Scene(raiz, 320, 320));
        ventana.setResizable(false);
        ventana.show();
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
