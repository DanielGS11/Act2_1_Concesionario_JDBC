import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GestorSQL {
    /*
    CONTENIDO DE 'propiedades':
    - Posicion 0: URL de MySQL (MySQl_url)
    - Posicion 1: URL de SQLite (SQLite_url)
    - Posicion 2: Path del CSV (path_CSV)
    - Posicion 3: Nombre del Usuario (user_name)
    - Posicion 4: Contraseña del Usuario (user_password)
    - Posicion 5: Ruta del Informe del concesionario (path_informe)
     */
    private List<String> propiedades = new ArrayList<>();

    //Aqui guardo las lineas del CSV de ejemplo para procesar y cargar en la Base de Datos
    private List<String> lineasCSV = new ArrayList<>();

    //Conexion a la Base de Datos MySQL o SQLite que se configurara en el metodo 'conectarBBDD()'
    private Connection conexion;

    //Metodo para conectarse a una Base de Datos, recoge la variable de que Base de Datos usara del menu de Main
    /**
     * @param opc: Opcion de conexion del usuario
     */
    public void conectarBBDD(String opc) {
        /*
        al principio llamara al metodo auxiliar que carga las propiedades de config.properties y
        recogera la URL de la Base de Datos, si todo sale bien, se informara al usuario
         */
        switch (opc) {
            case "1":
                cargarProperties();
                try {
                    /*
                    En el caso de conectarse a MySQL, la conexion establece el usuario y contraseña tambien
                     */
                    conexion = DriverManager.getConnection(propiedades.getFirst(),
                            propiedades.get(3), propiedades.get(4));

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

                    /*
                    Como en SQLite se necesita activar las Foreign Keys manualmente, el proceso se realiza
                    en el mismo momento en el que se conecta a la Base de Datos
                     */
                    Statement foreignKeys = conexion.createStatement();

                    foreignKeys.executeUpdate("PRAGMA foreign_keys = ON");
                } catch (SQLException e) {
                    System.out.println("Error de Conexion: " + e.getMessage());
                }
                break;

            default:
                System.out.println("Opcion no valida, debe ser 1 o 2");
                break;
        }
    }

    /*
    NOTA: A partir de aqui, todos los metodos llamaran al metodo auxiliar que comprueba que se haya establecido
    la conexion a una Base de Datos al inicio, informando al usuario y cancelando el metodo si no se ha conectado
     */

    //Metodo para crear las tablas si no estaban en la base de datos
    public void inicializarTablas() {
        if (!comprobarConexion()) {
            return;
        }

        /*
        Ya que SQLite y MySQL tienen ligeras diferencias en las sentencias de creacion de tablas,
        es necesario comprobar a que se conecto el usuario al crear las tablas y ver que sentencia usar.

        Los procesos, al estar predefinidos y sin intervencion del usuario, se añade a un lote de procesos para que
        una vez se creen todas las sentencias, se ejecuten en lote, aligerando la carga del programa
         */
        try {
            System.out.print("Inicializando tablas ");
            Statement tablas = conexion.createStatement();

            //MySQL
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
                System.out.println("Tablas inicializadas exitosamente");

                //SQLite
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
                System.out.println("Tablas inicializadas exitosamente");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Metodo para añadir un propietario a la Base de Datos, se recogeran los datos del propietario en el menu del Main

    /**
     * @param dni: DNI del propietario a añadir (OBLIGATORIO)
     * @param name: Nombre del propietario a añadir (OBLIGATORIO)
     * @param apellidos: Apellidos del propietario a añadir (OBLIGATORIO)
     * @param telefono: Telefono del propietario a añadir (OPCIONAL)
     */
    public void registrarPropietario(String dni, String name, String apellidos, String telefono) {
        if (!comprobarConexion()) {
            return;
        }

        //Primero se comprueba que los campos obligatorios esten llenos, para evitar errores
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

        /*
        Se busca si existe ya el propietario y si no, se insertan los valores recogidos
         */
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
            /*
            NOTA: Como puede no haber un telefono, el programa añade automaticamente 'null' a la Base de Datos si
            no se añadio un telefono
             */
            propietario.setString(4, telefono.isEmpty() ? null : telefono);

            propietario.executeUpdate();

            System.out.println("Propietario insertado correctamente");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Metodo para añadir un coche a la Base de Datos, se recogeran los datos del coche en el menu del Main
    /**
     * @param matricula: Matricula del coche a añadir (CAMPO CLAVE, OBLIGATORIO)
     * @param marca: Marca del coche a añadir (OBLIGATORIO)
     * @param modelo: Modelo del coche a añadir (OBLIGATORIO)
     * @param extras: Extras del coche a añadir (OPCIONAL)
     * @param precio: Precio del coche a añadir (OBLIGATORIO)
     */
    public void registrarCoche(String matricula, String marca, String modelo, String extras, double precio) {
        if (!comprobarConexion()) {
            return;
        }


        //Primero se comprueba que los campos obligatorios esten llenos, para evitar errores
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

        /*
        Se busca si existe ya el coche y si no, se insertan los valores recogidos
         */
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

            PreparedStatement coche = conexion.prepareStatement("INSERT INTO coches(matricula, marca, modelo, extras, precio) VALUES (?, ?, ?, ?, ?);");

            coche.setString(1, matricula);
            coche.setString(2, marca);
            coche.setString(3, modelo);
            //Extras puede estar vacio, asi que se asigna 'null' en la Base de Datos
            coche.setString(4, extras.isEmpty() ? null : extras);
            coche.setDouble(5, precio);

            coche.executeUpdate();

            System.out.println("Coche insertado correctamente");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /*
    Metodo para importar el CSV de ejemplo a la Base de Datos, aqui se llama tambien a los metodos auxiliares para
    cargar las propiedades de config.properties, del cual se sacara la ruta del CSV; y para leer las lineas de dicho
    CSV para procesarlas y añadirlas a la Base de Datos.

    NOTA: Esta operacion es transaccional, por lo que si falla un registro al insertarse, se cancela todo el proceso
     */
    public void importarCSV() {
        if (!comprobarConexion()) {
            return;
        }

        cargarProperties();
        leerCSV();

        System.out.printf("Importando '%s' a la base de datos...\n", propiedades.get(2));

        /*
        Ponemos el autocommit en false, lo que que hace que al ejecutar una sentencia, se quede en un buffer a la
        espera de confirmacion con commit, o cancelacion con rollback por si surge un error
         */
        try {
            conexion.setAutoCommit(false);
            /*
            Preparamos la sentencia para añadir un coche y procesamos cada linea del CSV para añadir los datos,
            comprobando en el proceso que los que no son obligatorios puedan estar vacios para añadir 'null' a la
            Base de Datos
             */
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
                /*
                NOTA: Al estar el precio en formato String en el CSV, se debe convertir a Double para que la
                Base de Datos lo lea
                 */
                coche.setDouble(5, Double.parseDouble(datosCoches.get(3).trim()));

                coche.executeUpdate();
            }

            //Si todo salio bien sin errores, se confirma la operacion con un commit
            conexion.commit();
            System.out.printf("Importacion de '%s' completada exitosamente\n", propiedades.get(2));
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());

            try {
                //Si hubo algun fallo durante las inserciones, se cancelara la operacion entera con rollback
                conexion.rollback();
                System.out.printf("Importacion cancelada con exito. Compruebe que no haya ningun coche en '%s' " +
                        "cuya matricula exista en la base de datos.\n", propiedades.get(2));
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } finally {
            try {
                /*
                en cualquier caso, si falla o tiene exito, se terminara volviendo a poner el autocommit en true
                para que deje de ser transaccional
                 */
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /*
    Metodo que listara los coches que pertenecen al concesionario,
    los cuales tienen el campo 'id_propietario' como 'null'
     */
    public void cochesDeConcesionario() {
        if (!comprobarConexion()) {
            return;
        }

        /*
        Filtramos todos los coches que tengan de propietario al concesionario en la sentencia, contamos cuantos son
        y se los mostraos al usuario
         */
        try {
            Statement cochesConcesionario = conexion.createStatement();

            ResultSet rs = cochesConcesionario.executeQuery("SELECT * FROM coches WHERE id_propietario IS NULL");

            int contadorCochesConcesionario = 0;

            while (rs.next()) {
                contadorCochesConcesionario++;

                List<String> coche = new ArrayList<>(List.of("Matricula (CLAVE): " + rs.getString("matricula"),
                        "\tMarca: " + rs.getString("marca"), "\tModelo: " + rs.getString("modelo"),
                        "\tExtras: " + rs.getString("extras"), "\tPrecio: " + rs.getString("precio"),
                        "\tPropietario: " + (rs.getString("id_propietario") == null ? "Concesionario" : rs.getString("id_propietario"))));

                coche.forEach(System.out::println);
            }

            //Puede no haber coches del concesionario, lo cual se informara al usuario
            if (contadorCochesConcesionario == 0) {
                System.out.println("No hay coches cuyo propietario sea el concesionario");
            } else {
                System.out.println("Total de coches cuyo propietario es el concesionario: " + contadorCochesConcesionario);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    //Metodo que listara los coches que tienen un propietario, dejando tambien la ID y DNI del mismo
    public void cochesConPropietario() {
        if (!comprobarConexion()) {
            return;
        }

        /*
        Filtramos todos los coches que tengan propietario en la sentencia, contamos cuantos son
        y se los mostraos al usuario
         */
        try {
            Statement cochesPropietario = conexion.createStatement();

            ResultSet rs = cochesPropietario.executeQuery("SELECT coches.*, propietarios.dni FROM coches JOIN propietarios ON " +
                    "propietarios.id_propietario = coches.id_propietario WHERE coches.id_propietario IS NOT NULL");

            int contadorCochesPropietario = 0;

            while (rs.next()) {
                contadorCochesPropietario++;

                List<String> coche = new ArrayList<>(List.of("Matricula (CLAVE): " + rs.getString("matricula"),
                        "\tMarca: " + rs.getString("marca"), "\tModelo: " + rs.getString("modelo"),
                        "\tExtras: " + rs.getString("extras"), "\tPrecio: " + rs.getString("precio"),
                        "\tID del Propietario: " + rs.getString("id_propietario"),
                        "\tDNI del Propietario: " + rs.getString("dni")));

                coche.forEach(System.out::println);
            }

            //Puede no haber coches con propietario, lo cual se informara al usuario
            if (contadorCochesPropietario == 0) {
                System.out.println("No hay coches con propietario");
            } else {
                System.out.println("Total de coches con propietario: " + contadorCochesPropietario);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /*
    Metodo que buscara el coche en la Base de Datos cuya matricula coincida con la introducida por el usuario
    en el menu de Main para modificarlo
     */
    /**
     * @param matricula: Matricula del coche a modificar (OBLIGATORIO)
     */
    public void modificarCoche(String matricula) {
        if (!comprobarConexion()) {
            return;
        }

        //Confirmamos que el coche exista en la base de datos, informando al usuario si no
        try {
            PreparedStatement coche = conexion.prepareStatement("SELECT * FROM coches WHERE matricula = ?");

            coche.setString(1, matricula);

            ResultSet rs = coche.executeQuery();

            /*
            Mostramos los datos del coche y pedimos los nuevos al usuario, sin modificar la matricula, que es su
            campo clave, y el id del propietario.

            El usuario puede decidir dejar un campo sin modificar pulsando intro, lo que hara que se asigne los datos
            que tenia ese campo originalmente, ademas que los campos nullables como 'extras' podra decidir si dejarlo
            igual, vacio, o con datos nuevos
             */
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
                        , matricula, marca, modelo, extras, precio,
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

    /*
    Metodo que buscara el coche en la Base de Datos cuya matricula coincida con la introducida por el usuario
    en el menu de Main para borrarlo
     */
    /**
     * @param matricula: Matricula del coche a borrar (OBLIGATORIO)
     */
    public void borrarCoche(String matricula) {
        if (!comprobarConexion()) {
            return;
        }

        //Verificamos que el coche con la matricula introducida exista y lo borramos de la Base de Datos
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

    /*
    Metodo que permite comprar un coche a otro propietario o al concesionario, se recogen el dni del comprador,
    la matricula del coche a comprar y el pago ofrecido en el menu del Main.

    NOTA: Esta operacion es transaccional, si falla algo, se cancela todo el proceso
     */
    /**
     * @param dni: DNI del comprador (OBLIGATORIO)
     * @param matricula: Matricula del coche a comprar (OBLIGATORIO)
     * @param pago: Pago del comprador por el coche (OBLIGATORIO)
     */
    public void realizarTraspaso(String dni, String matricula, double pago) {
        if (!comprobarConexion()) {
            return;
        }

        //Ponemos autocommit en false para hacerlo transaccional
        try {
            conexion.setAutoCommit(false);

            /*
            Primero buscaremos que existan tanto el coche a comprar como el cliente, ademas de que, si ambos existen,
            se pasara a comprobar que el pago sea suficiente
             */
            PreparedStatement coche = conexion.prepareStatement("SELECT * FROM coches WHERE matricula = ?");
            PreparedStatement cliente = conexion.prepareStatement("SELECT * FROM propietarios WHERE dni = ?");

            coche.setString(1, matricula);
            cliente.setString(1, dni);

            ResultSet cocheExists = coche.executeQuery();
            ResultSet clienteExists = cliente.executeQuery();

            if (cocheExists.next()) {

                if (clienteExists.next()) {
                    String vendedor = cocheExists.getString("id_propietario");

                    if (vendedor != null && vendedor.equalsIgnoreCase(clienteExists.getString("id_propietario"))) {
                        System.out.println("El coche ya fue adquirido por ese propietario");

                    } else {
                        if (Double.parseDouble(cocheExists.getString("precio")) > pago) {
                            System.out.println("Pago insuficiente, el coche cuesta " + cocheExists.getString("precio") + "€");

                        } else {
                            pago = cocheExists.getDouble("precio");
                            int comprador = clienteExists.getInt("id_propietario");

                            /*
                            ya que el vendedor puede ser el concesionario, por lo que 'id_propietario' del coche seria
                            'null', necesitamos 2 sentencias diferentes para el traspaso.

                            Se actualizara el propietario del coche al nuevo y se creara un registro de la transaccion
                             */
                            if (vendedor == null) {
                                PreparedStatement cocheTraspasado = conexion.prepareStatement("UPDATE coches set id_propietario = ? WHERE matricula = ? AND id_propietario IS NULL");
                                cocheTraspasado.setInt(1, comprador);
                                cocheTraspasado.setString(2, matricula);

                                PreparedStatement traspaso = conexion.prepareStatement("INSERT INTO traspasos(matricula_coche, id_vendedor, id_comprador, monto_economico) VALUES (?, null, ?, ?)");
                                traspaso.setString(1, matricula);
                                traspaso.setInt(2, comprador);
                                traspaso.setDouble(3, pago);

                                cocheTraspasado.executeUpdate();
                                traspaso.executeUpdate();

                            } else {
                                PreparedStatement cocheTraspasado = conexion.prepareStatement("UPDATE coches set id_propietario = ? WHERE matricula = ? AND id_propietario = ?");
                                cocheTraspasado.setInt(1, comprador);
                                cocheTraspasado.setInt(3, Integer.parseInt(vendedor));
                                cocheTraspasado.setString(2, matricula);

                                PreparedStatement traspaso = conexion.prepareStatement("INSERT INTO traspasos(matricula_coche, id_vendedor, id_comprador, monto_economico) VALUES (?, ?, ?, ?)");
                                traspaso.setString(1, matricula);
                                traspaso.setInt(2, Integer.parseInt(vendedor));
                                traspaso.setInt(3, comprador);
                                traspaso.setDouble(4, pago);

                                cocheTraspasado.executeUpdate();
                                traspaso.executeUpdate();
                            }

                            //Hecho todo con exito, se confirman los cambios con commit
                            conexion.commit();
                            System.out.println("Traspaso realizado con exito");
                        }
                    }
                } else {
                    System.out.printf("No se encontro un propietario con DNI '%s'\n", dni);
                }

            } else {
                System.out.printf("No se encontro un coche con matricula '%s'\n", matricula);
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());

            try {
                //En caso de fallar en alguna parte del proceso, se cancelara toda la operacion
                conexion.rollback();
                System.out.println("Traspaso cancelado con exito. Compruebe que existan tanto el dni del propietario " +
                        "como la matricula del coche en la base de datos,\nasi como que el precio que se paga es el correspondiente.\n");
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } finally {
            try {
                //En cualquiera de los dos casos, al terminar se pone autocommit en true para dejar de ser transaccional
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    //Metodo que llama al procedimiento almacenado (Procedure) de la Base de Datos
    public void ejecutarProcedure() {
        if (!comprobarConexion()) {
            return;
        }

        /*
        Este metodo solo funciona en MySQL, ya que SQLite no soporta procedimientos almacenados, Asi que se comprobara
        la conexion
         */
        if (!conexion.toString().contains("mysql")) {
            System.out.println("Por favor, conectese a la base de datos de MySQL para poder ejecutar esta accion");
            return;
        }

        /*
        Hecha la comprobacion, se ejecutara el Procedure, que debera estar guardado en la Base de Datos y se informara
        al usuario de las tablas creadas y/o modificadas al usuario.

        El Procedimiento Almacenado (Procedure) contiene tanto la creacion de las tablas como un par de registros
        de ejemplo para cada tabla
         */
        try {
            Statement procedure = conexion.createStatement();

            procedure.execute("CALL plantilla_tablas()");

            System.out.println("Procedure cargado con exito, tablas creadas/modificadas: ");

            Statement tablas = conexion.createStatement();

            ResultSet rs = tablas.executeQuery("SHOW TABLES;");

            while (rs.next()) {
                System.out.println(" - " + rs.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    /*
    Por ultimo, un Metodo para generar un informe de la base de datos, que contendra:
    - El numero de coches de la Base de Datos
    - Los coches agrupados por marca
    - El/Los equipamiento/s extra/s mas repetido/s en la Base de Datos
     */
    public void generarInforme() {
        if (!comprobarConexion()) {
            return;
        }

        //Cargaremos el config.properties para sacar la ruta del fichero del informe
        cargarProperties();

        //Recogemos los datos de los coches en una sentencia y creamos las variables necesarias
        try {
            Statement st = conexion.createStatement();

            ResultSet rs = st.executeQuery("select * from coches");

            //Aqui contamos los coches de la Base de Datos
            int contadorCoches = 0;

            //Aqu recogemos los datos de los coches y los agrupamos por marca
            Map<String, List<String>> cochesPorMarca = new HashMap<>();

            //Aqui recogemos todos los extras para despues ver cual se repite mas
            List<String> listaExtras = new ArrayList<>();
            while (rs.next()) {
                contadorCoches++;
                //NOTA: Los extras pueden estar vacion, por lo que simplemente se pone 'Ninguno'
                listaExtras.addAll(rs.getString("extras") == null ? List.of("Ninguno") :
                        List.of(rs.getString("extras").split(",")));

                //Aqui comprobamos si la marca ya se registro para no modificar los coches y añadir varios a la marca
                if (!cochesPorMarca.containsKey(rs.getString("marca"))) {
                    cochesPorMarca.put(rs.getString("marca"),
                            new ArrayList<>(List.of(String.format("""
                                            \t\t--- Matricula (CLAVE): %s
                                            \t\t\t---- Modelo: %s
                                            \t\t\t---- Extras: %s
                                            \t\t\t---- Precio: %s
                                            \t\t\t---- ID del Propietario: %s
                                            """, rs.getString("matricula"), rs.getString("modelo"),
                                    rs.getString("extras") == null ? "Ninguno" : rs.getString("extras"),
                                    rs.getString("precio"),
                                    //NOTA: si el id del propietario esta vacio, se pone que es propiedad del concesionario
                                    rs.getString("id_propietario") == null ? "Propiedad del Concesionario" :
                                            rs.getString("id_propietario"))))
                    );
                } else {
                    cochesPorMarca.get(rs.getString("marca")).add(String.format("""
                                    \t\t--- Matricula (CLAVE): %s
                                    \t\t\t---- Modelo: %s
                                    \t\t\t---- Extras: %s
                                    \t\t\t---- Precio: %s
                                    \t\t\t---- ID del Propietario: %s
                                    """, rs.getString("matricula"), rs.getString("modelo"),
                            rs.getString("extras") == null ? "Ninguno" : rs.getString("extras"),
                            rs.getString("precio"),
                            rs.getString("id_propietario") == null ? "Propiedad del Concesionario" :
                                    rs.getString("id_propietario")));
                }

            }

            /*
            Hecho todo, pasamos los coches agrupados por marca a un String para plasmar en el fichero
             */
            String cochesAgrupados = cochesPorMarca.entrySet().stream()
                    .map(e -> String.format("\t-- %s :\n%s\n", e.getKey(),
                            e.getValue().stream()
                                    .map(v -> String.format("%s", v)).collect(Collectors.joining())))
                    .collect(Collectors.joining());

            /*
            Creamos variables y el bucle que contara los extra y dira cual/es es/son el/los mas repetido/s
             */
            int repeticionEquipamiento = 0;
            StringBuilder equipamientoMasRepetido = new StringBuilder();

        /*
        recorremos la lista de extras y almacenamos temporalmente la palabra que toque y hacemos un contador que
        se incrementara cada vez que aparezca la palabra
         */
            for (int i = 0; i < listaExtras.size(); i++) {
                StringBuilder palabraTemp = new StringBuilder(listaExtras.get(i));
                AtomicInteger repeticionPalabraTemp = new AtomicInteger();

                listaExtras.forEach(s -> {
                    if (palabraTemp.toString().equals(s)) {
                        repeticionPalabraTemp.getAndIncrement();
                    }
                });

            /*
            Cada vez que termine de contar las repeticiones de una palabra, elimina todas las coincidencias de la lista,
            asi no se recorre mas veces de las necesarias
             */
                listaExtras.removeIf(s -> palabraTemp.toString().equals(s));

            /*
            Aqui comprobamos que solo se quede el extra que mas se repite y que, en caso de haber mas de 1, los ponga a
            los 2
             */
                if (repeticionPalabraTemp.get() > repeticionEquipamiento) {
                    equipamientoMasRepetido = new StringBuilder("\n\t" + palabraTemp);
                    repeticionEquipamiento = repeticionPalabraTemp.get();

                } else if (repeticionEquipamiento == repeticionPalabraTemp.get()) {
                    equipamientoMasRepetido.append("\n\t" + palabraTemp);
                }
            }

        /*
        Por ultimo, en caso de que no haya equipamientos o no se repita ninguno, el valor se plasmara en el informe
         */
            if (repeticionEquipamiento == 0) {
                equipamientoMasRepetido = new StringBuilder("Ningun Coche Dispone de Equipamiento");

            } else if (repeticionEquipamiento == 1) {
                equipamientoMasRepetido = new StringBuilder("Todos se repiten al menos 1 vez");
            } else {
                //Añadimos el contador al final para generalizar las veces que se repite un extra si hay varios
                equipamientoMasRepetido.append(String.format("\n\t--- Se repite(n) %d veces en la Base de Datos ---", repeticionEquipamiento));
            }

            //Aqui simplemente plasmaremos de que base de datos viene el informe
            String bbddDeInforme = "";

            if (conexion.toString().contains("sqlite")) {
                bbddDeInforme = "***Informe generado con SQLite***\n";
            } else if (conexion.toString().contains("mysql")) {
                bbddDeInforme = "***Informe generado con MySQL***\n";
            }

            //Configuradas todas las variables, las plasmamos en el fichero, que borrara su contenido cada vez que se haga
            Files.write(Paths.get(propiedades.getLast()), List.of(bbddDeInforme, "- Numero Total de Coches: " + contadorCoches,
                            "- Coches Agrupados Por Marca:\n " + cochesAgrupados,
                            "- Equipamiento que mas se repite: " + equipamientoMasRepetido + "\n"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.printf("informe '%s' generado con exito\n", propiedades.getLast());
        } catch (SQLException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /*
    ------------------------------------------------ METODOS AUXILIARES ------------------------------------------------
    */

    /*
    NOTA: Si ya se cargaron las propiedades o las lineas del CSV, los metodos auxiliares lo comprueban y no ejecutan
    todo el proceso, asi se ahorra carga innecesaria al programa al estar ya cargado
    anteriormente el csv/config.properties
     */

    //Metodo Auxiliar para cargar las propiedades de config.properties en la lista de propiedades
    private void cargarProperties() {
        if (propiedades.isEmpty()) {
            Properties prop = new Properties();

            try {
                //cargamos el archivo y vamos seleccionando las propiedades una a una y añadimos a la lista
                prop.load(new FileInputStream("config.properties"));

                propiedades.addAll(List.of(
                        prop.getProperty("MySQl_url"),
                        prop.getProperty("SQLite_url"),
                        prop.getProperty("path_CSV"),
                        prop.getProperty("user_name"),
                        prop.getProperty("user_password"),
                        prop.getProperty("path_informe")
                ));

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    //Metodo Auxiliar que lee las lineas del CSV para procesarlas, guardandolas en la lista de lineas
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

    //Metodo Auxiliar que comprueba si se ha hecho la conexion a una Base de Datos e informa en caso negativo
    private boolean comprobarConexion() {
        if (conexion == null) {
            System.out.println("Por favor, conectese primero a la base de datos");
            return false;
        } else {
            return true;
        }
    }
}
