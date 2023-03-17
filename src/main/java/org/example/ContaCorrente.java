package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class ContaCorrente {

    private String nomeTitular;
    private String agencia;
    private String conta;
    private String pix;
    private Map<LocalDate, String> extrato;
    private BigDecimal saldo = BigDecimal.ZERO;

    // Diretamente no caixa eletrônico:
    public void sacar(BigDecimal saque) {
        if (getSaldo().compareTo(saque) < 0) {
            System.out.println("Saldo insuficiente para saque! Por favor, digite uma quantia válida para saque.");
            return;
        }

        setSaldo(getSaldo().subtract(saque));
    }

    // Diretamente no caixa eletrônico:
    public void depositar(BigDecimal deposito) {
        setSaldo(getSaldo().add(deposito));
        String descricao = "DEPÓSITO: +" + deposito.toString();
        getExtrato().put(LocalDate.now(), descricao);
    }

    // Via pix
    public void depositar(String pix, BigDecimal deposito) {
        if (getPix().equals(pix)) {
            setSaldo(getSaldo().add(deposito));
            String descricao = "DEPÓSITO: +" + deposito.toString();
            getExtrato().put(LocalDate.now(), descricao);
            return;
        }
        System.out.println("Pix incorreto para efetuar o depósito! Por favor, insira seu pix corretamente.");
    }

    // Via depósito bancário usual
    public void depositar(String agencia, String conta, BigDecimal deposito) {
        if (getAgencia().equals(agencia) && getConta().equals(conta)) {
            setSaldo(getSaldo().add(deposito));
            String descricao = "DEPÓSITO: +" + deposito.toString();
            getExtrato().put(LocalDate.now(), descricao);
            return;
        }
        System.out.println("Dados inválidos para efetuar o depósito! Por favor, insira sua agência e conta bancária corretamente.");
    }

    // Método de visualização do extrato bancário
    public void verExtrato() {
        System.out.println("--- Extrato: ---");
        System.out.println("- DATA: - DESCRIÇÃO:");
        getExtrato().forEach((k, v) -> System.out.println(k + ": " + v));
    }

    public void verExtrato(LocalDate dataInicialDeVarredura) {
        if (LocalDate.now().isBefore(dataInicialDeVarredura)) {
            System.out.println("Data inválida! Por favor, insira uma data anterior ou igual a data de hoje (" + LocalDate.now() + ").");
            return;
        }

        System.out.println("--- Extrato: ---");
        System.out.println("- DATA: - DESCRIÇÃO:");
        getExtrato().forEach((k, v) -> {
            if (!k.isBefore(dataInicialDeVarredura)) System.out.println(k + ": " + v);
        });
    }

    // Pix entre contas correntes
    public void transferir(ContaCorrente corrente, String pix, BigDecimal valorTransferido) {
        if (valorTransferido.compareTo(getSaldo()) > 0) {
            System.out.println("Saldo insuficiente! Pix não autorizado.");
            return;
        }
        if (!corrente.getPix().equals(pix)) {
            System.out.println("Pix incorreto! Por favor, insira um pix válido.");
            return;
        }

        String descricao = " => DE " + getNomeTitular() + "; PARA " + corrente.getNomeTitular() + ";";

        corrente.setSaldo(corrente.getSaldo().add(valorTransferido));
        corrente.getExtrato().put(LocalDate.now(), "Pix recebido +" + valorTransferido + descricao);

        setSaldo(getSaldo().subtract(valorTransferido));
        getExtrato().put(LocalDate.now(), "Pix feito -" + valorTransferido + descricao);
    }

    // Transferência entre contas correntes
    public void transferir(ContaCorrente corrente, String agencia, String conta, BigDecimal valorTransferido) {
        if (valorTransferido.compareTo(getSaldo()) > 0) {
            System.out.println("Saldo insuficiente! Transferência não autorizada.");
            return;
        }
        if (!(corrente.getAgencia().equals(agencia) && corrente.getConta().equals(conta))) {
            System.out.println("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
            return;
        }

        String descricao = " => DE " + getNomeTitular() + "; PARA " + corrente.getNomeTitular() + ";";

        corrente.setSaldo(corrente.getSaldo().add(valorTransferido));
        corrente.getExtrato().put(LocalDate.now(), "Transferência recebida: +" + valorTransferido + descricao);

        setSaldo(getSaldo().subtract(valorTransferido));
        getExtrato().put(LocalDate.now(), "Transferência feita: -" + valorTransferido + descricao);
    }

    // Pix programado entre contas correntes
    public void transferir(LocalDate dataAgendada, ContaCorrente corrente, String pix, BigDecimal valorTransferido) {
        if (valorTransferido.compareTo(getSaldo()) > 0) {
            System.out.println("Saldo insuficiente! Pix não autorizado.");
            return;
        }
        if (!corrente.getPix().equals(pix)) {
            System.out.println("Pix incorreto! Por favor, insira um pix válido.");
            return;
        }

        String descricao = " => DE " + getNomeTitular() + "; PARA " + corrente.getNomeTitular() + ";";

        corrente.setSaldo(corrente.getSaldo().add(valorTransferido));
        corrente.getExtrato().put(dataAgendada, "Pix recebido +" + valorTransferido + descricao);

        setSaldo(getSaldo().subtract(valorTransferido));
        getExtrato().put(dataAgendada, "Pix feito -" + valorTransferido + descricao);
    }

    // Transferência programada entre contas correntes
    public void transferir(LocalDate dataAgendada, ContaCorrente corrente, String agencia, String conta, BigDecimal valorTransferido) {
        if (valorTransferido.compareTo(getSaldo()) > 0) {
            System.out.println("Saldo insuficiente! Transferência não autorizada.");
            return;
        }
        if (!(corrente.getAgencia().equals(agencia) && corrente.getConta().equals(conta))) {
            System.out.println("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
            return;
        }

        String descricao = " => DE " + getNomeTitular() + "; PARA " + corrente.getNomeTitular() + ";";

        corrente.setSaldo(corrente.getSaldo().add(valorTransferido));
        corrente.getExtrato().put(dataAgendada, "Transferência recebida: +" + valorTransferido + descricao);

        setSaldo(getSaldo().subtract(valorTransferido));
        getExtrato().put(dataAgendada, "Transferência feita: -" + valorTransferido + descricao);
    }
}
