package org.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Transacao implements Cloneable {

    @NonNull
    private LocalDateTime dataTransacao;
    @NonNull
    private TipoTransacao tipoTransacao;
    @NonNull
    @Setter
    private String descricao;

    @Override
    public Transacao clone() throws CloneNotSupportedException {

        try {
            super.clone();
            return new Transacao(this.dataTransacao, this.tipoTransacao, this.descricao);
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            throw new CloneNotSupportedException("Clonagem inválida!");
        }
    }
}
