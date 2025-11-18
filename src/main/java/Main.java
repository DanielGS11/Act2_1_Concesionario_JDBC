public class Main {
    public static void main(String[] args) {
        GestorSQL gestorSQL = new GestorSQL();

        gestorSQL.ConectarBBDD("1");
        gestorSQL.InicializarTabla();

        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");
        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");

        gestorSQL.ConectarBBDD("2");
        gestorSQL.InicializarTabla();

        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");
        gestorSQL.registrarPropietario("fhdjk", "dani", "garcia", "");
    }
}
