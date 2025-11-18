import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void InicializarTablas() {
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
                        "precio DECIMAL(10, 2), " +
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
                        "precio DECIMAL(10, 2), " +
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

        boolean camposSinRellenar = false;
        if (dni.isEmpty()) {
            System.out.println("Por favor, rellene el campo de DNI");
            camposSinRellenar = true;
        }
        if (name.isEmpty()) {
            System.out.println("Por favor, rellene el campo de Nombre");
            camposSinRellenar = true;
        }
        if (apellidos.isEmpty()) {
            System.out.println("Por favor, rellene el campo de Apellidos");
            camposSinRellenar = true;
        }

        if (camposSinRellenar) {
            return;
        }

        try {
            PreparedStatement comprobarPropietarios  = conexion.prepareStatement("SELECT * FROM propietarios WHERE dni= ? ");
            comprobarPropietarios.setString(1, dni);

            ResultSet rs = comprobarPropietarios.executeQuery();

            while (rs.next()) {
                if (rs.getString("dni").equalsIgnoreCase(dni)) {
                    System.out.printf("Ya existe un propietario con el dni '%s'\n", dni);
                    return;
                }
            }

            PreparedStatement propietario = conexion.prepareStatement("INSERT INTO propietarios(dni, nombre, apellidos, telefono) VALUES (?, ?, ?, ?);");

            propietario.setString(1, dni);
            propietario.setString(2, name);
            propietario.setString(3, apellidos);
            propietario.setString(4, telefono.isEmpty() ? null : telefono);

            propietario.executeUpdate();

            System.out.println("Propietario insertado correctamente");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public void registrarCoche(String matricula, String marca, String modelo, List<String> extras, double precio) {
        if (!comprobarConexion()) {
            return;
        }

        boolean camposSinRellenar = false;
        if (matricula.isEmpty()) {
            System.out.println("Por favor, rellene el campo de Matricula");
            camposSinRellenar = true;
        }
        if (marca.isEmpty()) {
            System.out.println("Por favor, rellene el campo de Marca");
            camposSinRellenar = true;
        }
        if (modelo.isEmpty()) {
            System.out.println("Por favor, rellene el campo de Modelo");
            camposSinRellenar = true;
        }
        if (precio <= 0) {
            System.out.println("Por favor, rellene el campo de Precio");
            camposSinRellenar = true;
        }

        if (camposSinRellenar) {
            return;
        }

        try {
            PreparedStatement comprobarCocheDuplicado  = conexion.prepareStatement("SELECT * FROM coches WHERE matricula= ? ");
            comprobarCocheDuplicado.setString(1, matricula);

            ResultSet rs = comprobarCocheDuplicado.executeQuery();

            while (rs.next()) {
                if (rs.getString("matricula").equalsIgnoreCase(matricula)) {
                    System.out.printf("Ya existe un coche con la matricula '%s'\n", matricula);
                    return;
                }
            }

            PreparedStatement coche = conexion.prepareStatement("INSERT INTO coches(matricula, marca, modelo, extras, precio) VALUES (?, ?, ?, ?, ?);");

            coche.setString(1, matricula);
            coche.setString(2, marca);
            coche.setString(3, modelo);
            coche.setString(4, extras.isEmpty() ? null : extras.toString());
            coche.setDouble(5, precio);

            coche.executeUpdate();

            System.out.println("Coche insertado correctamente");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void importarCSV() {
        if (!comprobarConexion()) {
            return;
        }

        cargarProperties();
        leerCSV();

        System.out.printf("Importando '%s' a la base de datos...\n", propiedades.get(2));

        try {
            conexion.setAutoCommit(false);
            PreparedStatement coche = conexion.prepareStatement("INSERT INTO coches(matricula, marca, modelo, extras, precio) VALUES (?, ?, ?, ?, ?);");

            for (String s : lineasCSV) {
                List<String> datosCoches = new ArrayList<>(List.of(s.trim().split(";")));

                if (datosCoches.size() == 4) {
                    datosCoches.add("null");
                }

                StringBuilder extras = new StringBuilder();
                Arrays.stream(datosCoches.get(4).trim().split("[|]")).toList()
                        .forEach(e -> extras.append(e).append(", "));

                extras.deleteCharAt(extras.lastIndexOf(","));

                coche.setString(1, datosCoches.getFirst().trim());
                coche.setString(2, datosCoches.get(1).trim());
                coche.setString(3, datosCoches.get(2).trim());
                coche.setString(4, (extras.toString().trim().equals("null") ? null : extras.toString()));
                coche.setDouble(5, Double.parseDouble(datosCoches.get(3).trim()));

                coche.executeUpdate();
            }

            conexion.commit();
            System.out.printf("Importacion de '%s' completada exitosamente\n", propiedades.get(2));
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());

            try {
                conexion.rollback();
                System.out.printf("Importacion cancelada con exito. Compruebe que no haya ningun coche en '%s' " +
                                "cuya matricula exista en la base de datos.\n", propiedades.get(2));
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void limpiarTablas() {
        if (!comprobarConexion()) {
            return;
        }

        try {
            Statement limpiarTablas = conexion.createStatement();

            limpiarTablas.addBatch("DELETE FROM propietarios;");
            limpiarTablas.addBatch("DELETE FROM coches;");
            limpiarTablas.addBatch("DELETE FROM traspasos;");

            limpiarTablas.executeBatch();
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
                lineasCSV.removeFirst();
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
