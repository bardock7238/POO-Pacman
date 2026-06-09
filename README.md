# POO-Pacman 🎮

Proyecto académico — Programación Orientada a Objetos 2026  
Universidad Distrital Francisco José de Caldas

## Requisitos

- **Java 17 o superior** ([descargar](https://adoptium.net))
- **Maven 3.8+** ([descargar](https://maven.apache.org/download.cgi)) — o usa el wrapper incluido

> No necesitas instalar JavaFX manualmente. Maven lo descarga solo.

## Cómo ejecutar

### Opción 1 — Desde terminal (recomendado)

```bash
# Clona o descomprime el proyecto, luego:
cd POO-Pacman
mvn javafx:run
```

### Opción 2 — Generar JAR ejecutable

```bash
mvn package
java -jar target/POO-Pacman.jar
```

### Opción 3 — VS Code

1. Abre la carpeta `POO-Pacman/` en VS Code
2. Instala la extensión **Extension Pack for Java**
3. Presiona `F5` o ve a **Run > Start Debugging** → configuración "Pac-Man (Maven)"

## Estructura del proyecto

```
POO-Pacman/
├── pom.xml                     ← Dependencias Maven (JavaFX incluido)
├── src/main/java/
│   ├── main.java
│   ├── Vista/
│   │   ├── MenuPrincipal.java  ← Menú JavaFX (punto de entrada)
│   │   ├── VentanaPrincipal.java
│   │   ├── JuegoPanel.java
│   │   └── Tablero.java
│   ├── Modelo/
│   │   ├── JuegoModelo.java
│   │   ├── PacMan.java
│   │   ├── Fantasma.java
│   │   ├── Laberinto.java
│   │   ├── Entidad.java
│   │   └── Movible.java
│   └── Controlador/
│       └── JuegoControlador.java
└── src/main/resources/
    └── puntajes.txt
```

## Controles

| Tecla | Acción |
|-------|--------|
| W / ↑ | Arriba |
| S / ↓ | Abajo |
| A / ← | Izquierda |
| D / → | Derecha |
| ENTER | Iniciar / Reanudar |
| P | Pausar |
| R | Reiniciar |
