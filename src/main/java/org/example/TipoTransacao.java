package org.example;

public enum TipoTransacao {

    SAQUE("Saque"),
    DEPOSITO("Depósito"),
    PIX("Pix"),
    TRANSFERENCIA("Transferência");

    private final String tipo;

    TipoTransacao(String tipoTransacao) {
        this.tipo = tipoTransacao;
    }

    @Override public String toString() {
        return tipo;
    }
}
