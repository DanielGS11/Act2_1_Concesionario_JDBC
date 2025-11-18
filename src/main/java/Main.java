import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        GestorSQL gestorSQL = new GestorSQL();


        gestorSQL.ConectarBBDD("1");
        gestorSQL.InicializarTablas();
        gestorSQL.limpiarTablas();

        gestorSQL.registrarPropietario("fhdjk", "", "garcia", "");
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", new ArrayList<>(), 100.70);
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", new ArrayList<>(), 100.70);
        gestorSQL.importarCSV();

        System.out.println("\n----------------------------------------------------------------------------------------\n");

        gestorSQL.ConectarBBDD("2");
        gestorSQL.InicializarTablas();
        gestorSQL.limpiarTablas();

        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", new ArrayList<>(), 100.70);
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", new ArrayList<>(), 100.70);
        gestorSQL.importarCSV();

    }
}
