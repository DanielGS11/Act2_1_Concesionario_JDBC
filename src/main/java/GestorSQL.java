import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
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
                    System.out.println("Error de Conexion: " + e.getMessage());
                }
                break;

            case "2":
                cargarProperties();
                try {
                    conexion = DriverManager.getConnection(propiedades.get(1));

                    System.out.println("Conexion establecida con la base de datos SQLite");
                } catch (SQLException e) {
                    System.out.println("Error de Conexion: " + e.getMessage());
                }
                break;

            default:
                System.out.println("Opcion no valida, debe ser 1 o 2");
                break;
        }
    }

    public void InicializarTabla() {
        if (!comprobarConexion()) {
            return;
        }

        try {
            System.out.print("Inicializando tablas ");
            Statement tablas = conexion.createStatement();

            if (conexion.toString().contains("mysql")) {
                System.out.println("MySQL");

                tablas.addBatch("CREATE TABLE IF NOT EXISTS propietarios(" +
                        "id_propietario INT AUTO_INCREMENT PRIMARY KEY, " +
                        "dni VARCHAR(10) NOT NULL, " +
                        "nombre VARCHAR(100) NOT NULL, " +
                        "apellidos VARCHAR(150) NOT NULL, " +
                        "telefono VARCHAR(15)" +
                        ");"
                );

                tablas.addBatch("CREATE TABLE IF NOT EXISTS coches(" +
                        "matricula VARCHAR(10) PRIMARY KEY NOT NULL, " +
                        "marca VARCHAR(50) NOT NULL, " +
                        "modelo VARCHAR(50) NOT NULL, " +
                        "extras VARCHAR(255), " +
                        "id_propietario INT," +
                        "FOREIGN KEY (id_propietario) REFERENCES propietarios(id_propietario)" +
                        ");"
                );

                tablas.addBatch("CREATE TABLE IF NOT EXISTS traspasos(" +
                        "id_traspaso INT AUTO_INCREMENT PRIMARY KEY, " +
                        "matricula_coche VARCHAR(10) NOT NULL, " +
                        "id_vendedor INT, " +
                        "id_comprador INT NOT NULL, " +
                        "monto_economico DECIMAL(10, 2) NOT NULL," +
                        "FOREIGN KEY (id_vendedor) REFERENCES propietarios(id_propietario)," +
                        "FOREIGN KEY (id_comprador) REFERENCES propietarios(id_propietario)," +
                        "FOREIGN KEY (matricula_coche) REFERENCES coches(matricula)" +
                        ");"
                );

                tablas.executeBatch();

            } else if (conexion.toString().contains("sqlite")) {
                System.out.println("SQLite");

                tablas.addBatch("CREATE TABLE IF NOT EXISTS propietarios(" +
                        "id_propietario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "dni VARCHAR(10) NOT NULL, " +
                        "nombre VARCHAR(100) NOT NULL, " +
                        "apellidos VARCHAR(150) NOT NULL, " +
                        "telefono VARCHAR(15)" +
                        ");"
                );

                tablas.addBatch("CREATE TABLE IF NOT EXISTS coches(" +
                        "matricula VARCHAR(10) PRIMARY KEY NOT NULL, " +
                        "marca VARCHAR(50) NOT NULL, " +
                        "modelo VARCHAR(50) NOT NULL, " +
                        "extras VARCHAR(255), " +
                        "id_propietario INTEGER," +
                        "FOREIGN KEY (id_propietario) REFERENCES propietarios(id_propietario)" +
                        ");"
                );

                tablas.addBatch("CREATE TABLE IF NOT EXISTS traspasos(" +
                        "id_traspaso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "matricula_coche VARCHAR(10) NOT NULL, " +
                        "id_vendedor INTEGER, " +
                        "id_comprador INTEGER NOT NULL, " +
                        "monto_economico DECIMAL(10, 2) NOT NULL," +
                        "FOREIGN KEY (id_vendedor) REFERENCES propietarios(id_propietario)," +
                        "FOREIGN KEY (id_comprador) REFERENCES propietarios(id_propietario)," +
                        "FOREIGN KEY (matricula_coche) REFERENCES coches(matricula)" +
                        ");"
                );

                tablas.executeBatch();
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void registrarPropietario(String dni, String name, String apellidos, String telefono) {
        if (!comprobarConexion()) {
            return;
        }

        try {
            Statement comprobarPropietarios  = conexion
            PreparedStatement propietario = conexion.prepareStatement("INSERT INTO propietarios(dni, nombre, apellidos, telefono) VALUES (?, ?, ?, ?);");

            propietario.setString(1, dni);
            propietario.setString(2, name);
            propietario.setString(3, apellidos);
            propietario.setString(4, telefono.isEmpty() ? null : telefono);

            propietario.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
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

    private boolean comprobarConexion() {
        if (conexion == null) {
            System.out.println("Por favor, conectese primero a la base de datos");
            return false;
        } else {
            return true;
        }
    }
}
