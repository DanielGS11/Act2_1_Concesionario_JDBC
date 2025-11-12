public class Transaccion {
    private int id;
    private String matriculaCoche;
    private int idVendedor;
    private int idComprador;
    private double monto;

    public Transaccion(String matriculaCoche, int idVendedor, int idComprador, double monto) {
        this.matriculaCoche = matriculaCoche;
        this.idVendedor = idVendedor;
        this.idComprador = idComprador;
        this.monto = monto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMatriculaCoche() {
        return matriculaCoche;
    }

    public int getIdVendedor() {
        return idVendedor;
    }

    public int getIdComprador() {
        return idComprador;
    }

    public double getMonto() {
        return monto;
    }

    public String toString() {
        return String.format("\tID: %d\t\nMatricula del Coche: %s\t\nID del Vendedor: %d\t\nID del Comprador: %d\t\nMonto Economico: %.2f",
                id, matriculaCoche, idVendedor == 0 ? null: idVendedor, idComprador, monto);
    }
}
