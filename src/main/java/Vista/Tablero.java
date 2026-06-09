package Vista;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Tablero {

    private static final String ARCHIVO = "puntajes.txt";
    private static final int MAX_ENTRADAS = 5;

    public static void guardarPuntaje(String nombre, int puntos) {
        List<int[]> lista = cargarRaw();
        List<String> nombres = cargarNombres();

        int pos = lista.size();
        for (int i = 0; i < lista.size(); i++) {
            if (puntos > lista.get(i)[0]) { pos = i; break; }
        }
        lista.add(pos, new int[]{puntos});
        nombres.add(pos, nombre + "  [" + LocalDate.now() + "]");

        while (lista.size() > MAX_ENTRADAS) {
            lista.remove(lista.size() - 1);
            nombres.remove(nombres.size() - 1);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (int i = 0; i < lista.size(); i++) {
                pw.println(lista.get(i)[0] + "|" + nombres.get(i));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> cargarPuntajes() {
        List<String> resultado = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return resultado;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|", 2);
                if (partes.length == 2) {
                    resultado.add(partes[0] + " pts — " + partes[1]);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return resultado;
    }

    private static List<int[]> cargarRaw() {
        List<int[]> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|", 2);
                if (partes.length >= 1) {
                    try { lista.add(new int[]{Integer.parseInt(partes[0].trim())}); }
                    catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ex) { ex.printStackTrace(); }
        return lista;
    }

    private static List<String> cargarNombres() {
        List<String> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return lista;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|", 2);
                lista.add(partes.length == 2 ? partes[1] : "Jugador");
            }
        } catch (IOException ex) { ex.printStackTrace(); }
        return lista;
    }
}