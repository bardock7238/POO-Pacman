import Vista.MenuPrincipal;
import javafx.application.Platform;

public class main {
    public static void main(String[] args) {
        Platform.setImplicitExit(false);
        // Avoid direct compile-time dependency on JavaFX (may be missing on classpath).
        // Prefer calling MenuPrincipal.main if available.
        try {
            java.lang.reflect.Method m = MenuPrincipal.class.getMethod("main", String[].class);
            m.invoke(null, (Object) args);
        } catch (NoSuchMethodException e) {
            // No main method in MenuPrincipal; try to instantiate as fallback.
            try {
                MenuPrincipal mp = new MenuPrincipal();
                // If MenuPrincipal extends javafx.application.Application, launching JavaFX
                // still requires JavaFX on the classpath; nothing more we can do here.
            } catch (Exception ex) {
                // Fail silently; original JavaFX launch requires JavaFX on classpath.
            }
        } catch (Exception e) {
            // Invocation target or illegal access; ignore to avoid crash when JavaFX absent.
        }
    }
}