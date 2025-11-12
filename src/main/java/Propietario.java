public class Propietario {
    private int id;
    private String dni;
    private String nombre;
    private String apellidos;
    private String telefono;

    public Propietario(String dni, String nombre, String apellidos, String telefono) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getTelefono() {
        //NOTA: Si telefono esta vacio, se plasma 'Ninguno' en el campo
        return telefono.isEmpty() ? "Ninguno" : telefono;
    }

    @Override
    public String toString() {
        return String.format("\tID: %d\t\nDNI: %s\t\nNombre: %s\t\nApellidos: %s\t\nTelefono: %s",
                id, dni, nombre, apellidos, telefono);
    }
}
