package org.example;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NonNull
@Getter
@Setter
public class Transacao implements Cloneable {

    private LocalDateTime dataTransacao;
    private TipoTransacao tipoTransacao;
    private String descricao;

    public String formatarDataEHora(String data) {
        if (data.length() < 2) return "0" + data;

        return data;
    }

    public String formatarParaExtrato() {
        return formatarDataEHora(dataTransacao.getDayOfMonth() + "") + "/" + formatarDataEHora(dataTransacao.getMonthValue() + "") + "/"
                + dataTransacao.getYear() + " " + formatarDataEHora(dataTransacao.getHour() + "") + ":"
                + formatarDataEHora(dataTransacao.getMinute() + "") + " - " + tipoTransacao + ": " + descricao;
    }

    @Override
    public Transacao clone() throws CloneNotSupportedException {

        try {
            super.clone();
            return new Transacao(this.dataTransacao, this.tipoTransacao, this.descricao);
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            throw new CloneNotSupportedException("Ops! Não foi possível realizar a transação. Por favor, tente novamente mais tarde.");
        }
    }
}
