import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        GestorSQL gestorSQL = new GestorSQL();


        gestorSQL.ConectarBBDD("1");
        gestorSQL.limpiarTablas();
        gestorSQL.InicializarTablas();
        gestorSQL.cochesDeConcesionario();
        gestorSQL.cochesConPropietario();
        System.out.println("\n");

        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");
        gestorSQL.registrarPropietario("fgdjk", "", "garcia", "");
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", "", 100.70);
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", "", 100.70);
        gestorSQL.importarCSV();
        System.out.println("\n");

        gestorSQL.cochesDeConcesionario();
        System.out.println("\n");

        gestorSQL.cochesConPropietario();

        System.out.println("\n----------------------------------------------------------------------------------------\n");

        gestorSQL.ConectarBBDD("2");
        gestorSQL.limpiarTablas();
        gestorSQL.InicializarTablas();
        System.out.println("\n");

        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");
        gestorSQL.registrarPropietario("fddjk", "dani", "garcia", "");
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", "aa", 100.70);
        gestorSQL.registrarCoche("8530", "fjsal", "hfao", "" , 100.70);

        gestorSQL.importarCSV();
        System.out.println("\n");

        gestorSQL.borrarCoche("8530");



    }
}
