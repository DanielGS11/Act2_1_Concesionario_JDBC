import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        //Creamos el gestorSQL para acceder a sus operaciones y el escaner para introducir datos y los pasamos al menu
        GestorSQL gestorSQL = new GestorSQL();
        Scanner sc = new Scanner(System.in);

        menu(sc, gestorSQL);
    }

    /**
     * @param sc: Scanner para recoger datos por pantalla
     * @param gestorSQL:Gestor con las sentencias y metodos SQL
     */
    private static void menu(Scanner sc, GestorSQL gestorSQL) {
        //ofrecemos las opciones al usuario y le damos a elegir para que introduzca por pantalla, llamando al menu de opciones
        System.out.println("*********** BIENVENIDO AL GESTOR DE BBDD ***********");

        System.out.print("""
                - 1) Conectar a la Base de Datos (MySQL o SQLite)
                - 2) Inicializar/Crear Tablas de la Base de Datos
                - 3) Registrar un Propietario en la Base de Datos
                - 4) Registrar un Coche en la Base de Datos
                - 5) Importar Archivo CSV (BBDDejemplo.csv) a la Base de Datos
                - 6) Listar Coches sin Propietario de la Base de Datos
                - 7) Listar Coches con Propietario de la Base de Datos
                - 8) Modificar un Coche de la Base de Datos
                - 9) Borrar un Coche de la Base de Datos
                - 10) Realizar un Traspaso de Coche a un Propietario de la Base de Datos
                - 11) Ejecutar Procedimiento Almacenado de la Base de Datos (Solo en MySQL)
                - 12) Generar un Informe de la Base de Datos
                - 13) Salir
                
                IMPORTANTE: Es recomendable realizar la conexion a la Base de Datos para poder operar en ella
                Introduzca la Opcion a Elegir (Introduzca el numero):\s
                """);

        String opc = sc.nextLine();

        optionsMenu(sc, opc, gestorSQL);
    }

    /**
     * @param sc: Scanner para recoger datos por pantalla
     * @param gestorSQL:Gestor con las sentencias y metodos SQL
     * @param opc: Opcion del menu dada por el usuario
     */
    private static void optionsMenu(Scanner sc, String opc, GestorSQL gestorSQL) {
         /*
        Creamos un switch que, dependiendo de la opcion, la ejecute o, si el usuario se equivoco, se lo haga saber,
        ademas de una opcion para salir del programa.
        Tras cada ejecucion de un metodo o equivocacion del usuario, excepto si decide salir, volvera al metodo menu
        para elegir mas opciones
        Algunas operaciones pediran datos adicionales al usuario, como que Base de Datos conectar, o credenciales del
        propetario o coche a añadir o modificar
         */
        switch (opc) {
            case "1":
                System.out.print("""
                        - 1) MySQL
                        - 2) SQLite
                        
                        ¿A qué Base de Datos le Gustaria COnectarse? (Introduzca el numero):\s""");

                gestorSQL.conectarBBDD(sc.nextLine());
                break;

            case "2":
                gestorSQL.inicializarTablas();
                break;

            case "3":
                System.out.println("Introduzca los datos del Propietario que se pediran a continuacion");
                System.out.print("DNI: ");
                String dni = sc.nextLine().toUpperCase();

                System.out.print("Nombre: ");
                String nombre = sc.nextLine();
                nombre = nombre.toUpperCase().charAt(0) + nombre.toLowerCase().substring(1);

                System.out.print("Apellidos: ");
                String apellido = sc.nextLine();
                if (apellido.split(" ").length == 2) {
                    apellido = apellido.toUpperCase().charAt(0) + apellido.toLowerCase().substring(1, apellido.indexOf(" ") + 1)
                            + apellido.toUpperCase().charAt(apellido.indexOf(" ") + 1) + apellido.toLowerCase().substring(apellido.indexOf(" ") + 2);
                } else {
                    apellido = apellido.toUpperCase().charAt(0) + apellido.toLowerCase().substring(1);
                }

                System.out.print("Telefono (Opcional, si no se quiere introducir pulse intro): ");
                String telefono = sc.nextLine();

                gestorSQL.registrarPropietario(dni, nombre, apellido, telefono);
                break;

            case "4":
                System.out.println("Introduzca los datos del Coche que se pediran a continuacion");
                System.out.print("Matricula: ");
                String matricula = sc.nextLine().toUpperCase();

                System.out.print("Marca: ");
                String marca = sc.nextLine();
                marca = marca.toUpperCase().charAt(0) + marca.toLowerCase().substring(1);

                System.out.print("Modelo: ");
                String modelo = sc.nextLine();

                System.out.print("Extras (Opcional, si no se quiere introducir pulse intro.\n" +
                        "Ponga los extras en una misma linea): ");
                String extras = sc.nextLine();

                System.out.print("Precio: ");
                double precio = Double.parseDouble(sc.nextLine());

                gestorSQL.registrarCoche(matricula, marca, modelo, extras, precio);
                break;

            case "5":
                gestorSQL.importarCSV();
                break;

            case "6":
                gestorSQL.cochesDeConcesionario();
                break;

            case "7":
                gestorSQL.cochesConPropietario();
                break;

            case "8":
                System.out.print("Introduzca la matricula del coche a modificar: ");
                gestorSQL.modificarCoche(sc.nextLine().toUpperCase());
                break;

            case "9":
                System.out.print("Introduzca la matricula del coche a borrar: ");
                gestorSQL.borrarCoche(sc.nextLine().toUpperCase());
                break;

            case "10":
                System.out.println("Introduzca los datos que se pediran a continuacion: ");
                System.out.print("DNI del comprador: ");
                String dniComprador = sc.nextLine().toUpperCase();

                System.out.print("Matricula del Coche a comprar: ");
                String matriculaCocheComprar = sc.nextLine().toUpperCase();

                System.out.print("Pago del Coche: ");
                double pago = Double.parseDouble(sc.nextLine());

                gestorSQL.realizarTraspaso(dniComprador, matriculaCocheComprar, pago);
                break;

            case "11":
                gestorSQL.ejecutarProcedure();
                break;

            case "12":

                gestorSQL.generarInforme();
                break;

            case "13":
                System.out.println("Gracias por usar nuestros servicios ¡Tenga un Buen Dia!");
                return;

            default:
                System.out.println("""
                        -------------------------------------------------
                          Por favor, elija un numero válido del 1 al 13
                        -------------------------------------------------""");
                break;
        }
        menu(sc, gestorSQL);
    }
}
