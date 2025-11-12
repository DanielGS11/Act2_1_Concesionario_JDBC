import java.util.ArrayList;
import java.util.List;

public class Coche {
    private String matricula;

    private String marca;

    private String modelo;

    private double precio;

    private List<String> equipamiento;

    private int idPropietario;

    public Coche(String matricula, String marca, String modelo, double precio, List<String> equipamiento, int idPropietario) {
        this.matricula = matricula.toUpperCase();
        this.marca = marca.toUpperCase().charAt(0) + marca.toLowerCase().substring(1, marca.length());
        this.modelo = modelo.toUpperCase().charAt(0) + modelo.toLowerCase().substring(1, modelo.length());
        this.precio = precio;
        this.equipamiento = equipamiento.isEmpty() ? List.of("Ninguno") : equipamiento;
        this.idPropietario = idPropietario;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public double getPrecio() {
        return precio;
    }

    public List<String> getEquipamiento() {
        return equipamiento;
    }

    public int getIdPropietario() {
        return idPropietario;
    }

    //Metodo toString para la clase coche que tambien servira para generar el informe
    @Override
    public String toString() {
        return String.format("\tMatrícula: %s\n\tMarca: %s\n\tModelo: %s\n\tPrecio: %.2f\n\tEquipamiento Incluido: %s\n\tID del Propietario: %d\n",
                matricula, marca, modelo, precio, equipamiento.toString(), idPropietario == 0 ? null: idPropietario);
    }
}
