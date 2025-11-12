import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GestorSQL {
    private Concesionario concesionario = new Concesionario();

    /*
    CONTENIDO DE 'propiedades':
    - Posicion 0: URL de MySQL (MySQl_url)
    - Posicion 1: URL de SQLite (SQLite_url)
    - Posicion 2: Path del CSV (path_CSV)
    - Posicion 3: Nombre del Usuario (user_name)
    - Posicion 4: Contraseña del Usuario (user_password)
     */
    private List<String> propiedades = new ArrayList<>();

    private List<String> lineasCSV = new ArrayList<>();

    private Connection conexion;

    public void ConectarBBDD(String opc) {
        switch (opc) {
            case "1":
                cargarProperties();
                try {
                    conexion = DriverManager.getConnection(propiedades.getFirst(),
                            propiedades.get(3), propiedades.getLast());

                    System.out.println("Conexion establecida con la base de datos MySQL");
                } catch (SQLException e) {
                    System.out.println("Error de Conexion: " +  e.getMessage());
                }
                break;

            case "2":
                cargarProperties();
                try {
                    conexion = DriverManager.getConnection(propiedades.get(1));

                    System.out.println("Conexion establecida con la base de datos SQLite");
                } catch (SQLException e) {
                    System.out.println("Error de Conexion: " +  e.getMessage());
                }
                break;

            default:
                System.out.println("Opcion no valida, debe ser 1 o 2");
                break;
        }
    }

    /*
    ------------------------------------------------ METODOS AUXILIARES ------------------------------------------------
    */

    private void cargarProperties() {
        if (propiedades.isEmpty()) {
            Properties prop = new Properties();

            try {
                prop.load(new FileInputStream("config.properties"));

                propiedades.addAll(List.of(
                        prop.getProperty("MySQl_url"),
                        prop.getProperty("SQLite_url"),
                        prop.getProperty("path_CSV"),
                        prop.getProperty("user_name"),
                        prop.getProperty("user_password")
                ));

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void leerCSV() {
        if (lineasCSV.isEmpty()) {
            try {
                cargarProperties();
                lineasCSV = Files.readAllLines(Paths.get(propiedades.get(2)));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }


}
