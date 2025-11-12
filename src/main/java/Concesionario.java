import java.util.ArrayList;
import java.util.List;

public class Concesionario {
    private List<Coche> coches = new ArrayList<>();
    private List<Propietario> propietarios = new ArrayList<>();
    private List<Transaccion> transacciones = new ArrayList<>();

    public List<Coche> getCoches() {
        return coches;
    }

    public List<Propietario> getPropietarios() {
        return propietarios;
    }

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }
}
