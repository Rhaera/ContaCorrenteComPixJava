package org.example;

public enum TipoTransacao {

    SAQUE("Saque"),
    DEPOSITO("Deposito"),
    PIX("Pix"),
    TRANSFERENCIA("Transferencia");

    private final String tipo;

    TipoTransacao(String tipoTransacao) {
        this.tipo = tipoTransacao;
    }

    @Override
    public String toString() {
        return tipo;
    }
}
