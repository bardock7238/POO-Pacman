package Modelo;

/**
 * Clase abstracta base para todas las entidades del juego.
 * Aplica abstracción y encapsulamiento.
 */
public abstract class Entidad {

    protected int x;
    protected int y;
    protected int velocidad;
    protected boolean activo;

    public Entidad(int x, int y, int velocidad) {
        this.x = x;
        this.y = y;
        this.velocidad = velocidad;
        this.activo = true;
    }

    // Método abstracto que cada subclase debe implementar
    public abstract void mover(Laberinto laberinto);

    public abstract void reiniciar();

    // Getters y Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getVelocidad() { return velocidad; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
