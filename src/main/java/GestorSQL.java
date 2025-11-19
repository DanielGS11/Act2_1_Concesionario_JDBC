import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

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

                    Statement foreignKeys = conexion.createStatement();

                    foreignKeys.executeQuery("PRAGMA foreign_keys = ON");
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
            PreparedStatement comprobarPropietarios = conexion.prepareStatement("SELECT * FROM propietarios WHERE dni= ? ");
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


    public void registrarCoche(String matricula, String marca, String modelo, String extras, double precio) {
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
            PreparedStatement comprobarCocheDuplicado = conexion.prepareStatement("SELECT * FROM coches WHERE matricula= ? ");
            comprobarCocheDuplicado.setString(1, matricula);

            ResultSet rs = comprobarCocheDuplicado.executeQuery();

            while (rs.next()) {
                if (rs.getString("matricula").equalsIgnoreCase(matricula)) {
                    System.out.printf("Ya existe un coche con la matricula '%s'\n", matricula);
                    return;
                }
            }

            PreparedStatement coche = conexion.prepareStatement("INSERT INTO coches(matricula, marca, modelo, extras, precio, id_propietario) VALUES (?, ?, ?, ?, ?, 1);");

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

    public void cochesDeConcesionario() {
        if (!comprobarConexion()) {
            return;
        }

        try {
            Statement cochesConcesionario = conexion.createStatement();

            ResultSet rs = cochesConcesionario.executeQuery("SELECT * FROM coches WHERE id_propietario IS NULL");

            int contadorCochesConcesionario = 0;

            while (rs.next()) {
                contadorCochesConcesionario++;

                List<String> coche = new ArrayList<>(List.of("Matricula (CLAVE): " + rs.getString("matricula"),
                        "\tMarca: " + rs.getString("marca"), "\tModelo: " + rs.getString("modelo"),
                        "\tExtras: " + rs.getString("matricula"), "\tPrecio: " + rs.getString("precio"),
                        "\tPropietario: " + (rs.getString("id_propietario") == null ? "Concesionario" : rs.getString("id_propietario"))));

                coche.forEach(System.out::println);
            }

            if (contadorCochesConcesionario == 0) {
                System.out.println("No hay coches cuyo propietario sea el concesionario");
            } else {
                System.out.println("Total de coches cuyo propietario es el concesionario: " + contadorCochesConcesionario);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void cochesConPropietario() {
        if (!comprobarConexion()) {
            return;
        }

        try {
            Statement cochesPropietario = conexion.createStatement();

            ResultSet rs = cochesPropietario.executeQuery("SELECT coches.*, propietarios.dni FROM coches JOIN propietarios ON " +
                    "propietarios.id_propietario = coches.id_propietario WHERE coches.id_propietario IS NOT NULL");

            int contadorCochesPropietario = 0;

            while (rs.next()) {
                contadorCochesPropietario++;

                List<String> coche = new ArrayList<>(List.of("Matricula (CLAVE): " + rs.getString("matricula"),
                        "\tMarca: " + rs.getString("marca"), "\tModelo: " + rs.getString("modelo"),
                        "\tExtras: " + rs.getString("matricula"), "\tPrecio: " + rs.getString("precio"),
                        "\tPropietario: " + rs.getString("id_propietario"),
                        "\tDNI del Propietario: " + rs.getString("dni")));

                coche.forEach(System.out::println);
            }

            if (contadorCochesPropietario == 0) {
                System.out.println("No hay coches con propietario");
            } else {
                System.out.println("Total de coches con propietario: " + contadorCochesPropietario);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void modificarCoche(String matricula) {
        if (!comprobarConexion()) {
            return;
        }

        try {
            PreparedStatement coche = conexion.prepareStatement("SELECT * FROM coches WHERE matricula = ?");

            coche.setString(1, matricula);

            ResultSet rs = coche.executeQuery();

            if (rs.next()) {
                Scanner sc = new Scanner(System.in);

                String marca = rs.getString("marca");
                String modelo = rs.getString("modelo");
                String extras = rs.getString("extras") == null ? "Ninguno" : rs.getString("extras");
                String precio = rs.getString("precio");
                String id_propietario = rs.getString("id_propietario") == null ? "Concesionario" : rs.getString("id_propietario");

                System.out.printf("""
                                Estos son los datos del coche con matricula '%s':
                                \tMarca: %s
                                \tModelo: %s
                                \tExtras: %s
                                \tPrecio: %s
                                \tID del Propietario: %s
                                """
                        ,matricula, marca, modelo, extras, precio,
                        id_propietario.equals("Concesionario") ? "Propiedad del Concesionario" : id_propietario
                );

                System.out.println("Introduzca la nueva Marca del coche\n(Si no quiere cambiarla, pulse intro)");
                String newMarca = sc.nextLine();

                if (!newMarca.isEmpty()) {
                    marca = newMarca.toUpperCase().charAt(0) + newMarca.trim().toLowerCase().substring(1, newMarca.length());
                }

                System.out.println("Introduzca el nuevo Modelo del coche\n(Si no quiere cambiarlo, pulse intro)");
                String newModelo = sc.nextLine();

                if (!newModelo.isEmpty()) {
                    modelo = newModelo.toUpperCase().charAt(0) + newModelo.trim().toLowerCase().substring(1, newModelo.length());
                }

                System.out.println("Introduzca los nuevos Extras del coche\n(Si no quiere cambiarlo, pulse intro" +
                        " o, si el coche no lleva extras, escriba 'nada')");
                String newExtras = sc.nextLine();

                if (newExtras.trim().equalsIgnoreCase("nada")) {
                    extras = "Ninguno";
                } else if (!newExtras.isEmpty()) {
                    extras = newExtras.trim();
                }

                System.out.println("Introduzca el nuevo Precio del coche\n[Si lleva decimales, pongalo con un '.'] " +
                        "(Si no quiere cambiarlo, pulse intro)");
                String newPrecio = sc.nextLine();

                if (!newPrecio.isEmpty()) {
                    precio = newPrecio.trim();
                }

                PreparedStatement newCoche = conexion.prepareStatement("UPDATE coches " +
                        "SET marca = ?," +
                        "modelo = ?," +
                        "extras = ?," +
                        "precio = ?" +
                        "WHERE matricula = ?");

                newCoche.setString(1, marca);
                newCoche.setString(2, modelo);
                newCoche.setString(3, extras);
                newCoche.setDouble(4, Double.parseDouble(precio));
                newCoche.setString(5, matricula);

                newCoche.executeUpdate();
                System.out.println("Coche modificado con exito");
            } else {
                System.out.printf("No se encontro un coche con matricula '%s'\n", matricula);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void borrarCoche(String matricula) {
        if (!comprobarConexion()) {
            return;
        }

        try {
            PreparedStatement coche = conexion.prepareStatement("SELECT * FROM coches WHERE matricula = ?");

            coche.setString(1, matricula);

            ResultSet rs = coche.executeQuery();

            if (rs.next()) {
                PreparedStatement borrarCoche = conexion.prepareStatement("DELETE FROM coches WHERE matricula = ?");

                borrarCoche.setString(1, matricula);

                borrarCoche.executeUpdate();
                System.out.println("Coche borrado con exito");
            } else {
                System.out.printf("No se encontro un coche con matricula '%s'\n", matricula);
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void limpiarTablas() {
        if (!comprobarConexion()) {
            return;
        }

        try {
            Statement limpiarTablas = conexion.createStatement();

            limpiarTablas.addBatch("DROP TABLE IF EXISTS traspasos;");

            limpiarTablas.addBatch("DROP TABLE IF EXISTS coches;");


            limpiarTablas.addBatch("DROP TABLE IF EXISTS propietarios");


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
