package org.example;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContaCorrente {

    @NonNull
    private final String agencia;
    @NonNull
    private final String conta;
    private String nomeTitular;
    @Getter
    private String pix;
    private Map<LocalDateTime, String> extrato;
    private List<Transacao> extrato2;
    @Getter
    private BigDecimal saldo;

    // Estrutura de dados estática para armazenar os clientes
    private static final List<ContaCorrente> listaClientes = new ArrayList<>();

    // Builder implementation
    public static class AccountBuilder {

        private final String agencia;
        private final String conta;
        private String nomeTitular = "";
        private String pix = "";
        private final BigDecimal saldo = BigDecimal.ZERO;
        private final Map<LocalDateTime, String> extrato = new HashMap<>();
        private final List<Transacao> extrato2 = new ArrayList<>();

        public AccountBuilder(String agencia, String conta) {
            this.agencia = agencia;
            this.conta   = conta;
        }

        public AccountBuilder nomeTitular(String nome) {
            this.nomeTitular = nome;
            return this;
        }

        public AccountBuilder pix(String pix) {
            this.pix = pix;
            return this;
        }

        public ContaCorrente build() {
            listaClientes.add(new ContaCorrente(this)); // Certo!
            return new ContaCorrente(this);
        }
    }

    private ContaCorrente(AccountBuilder aBuilder) {

        this.agencia     = aBuilder.agencia;
        this.conta       = aBuilder.conta;
        this.nomeTitular = aBuilder.nomeTitular;
        this.pix         = aBuilder.pix;
        this.saldo       = aBuilder.saldo;
        this.extrato     = aBuilder.extrato;
        this.extrato2    = aBuilder.extrato2;

        // listaClientes.add(new ContaCorrente(aBuilder)); -> Errado!
    }

    // Diretamente no caixa eletrônico:
    public void sacar(BigDecimal saque) {
        if (this.saldo.compareTo(saque) < 0) {
            System.out.println("Saldo insuficiente para saque! Por favor, digite uma quantia válida para saque.");
            return;
        }

        String descricao = "SAQUE: -" + saque.toString() + ";";

        Transacao transacao = new Transacao(LocalDateTime.now(), TipoTransacao.SAQUE, descricao);

        saldo = this.saldo.subtract(saque);
        extrato.put(LocalDateTime.now(), descricao);
        extrato2.add(transacao);
        listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
    }

    // Diretamente no caixa eletrônico:
    public void depositar(BigDecimal deposito) {
        saldo = this.saldo.add(deposito);
        String descricao = "DEPÓSITO: +" + deposito.toString();
        extrato.put(LocalDateTime.now(), descricao);
        Transacao transacao = new Transacao(LocalDateTime.now(), TipoTransacao.DEPOSITO, descricao);
        extrato2.add(transacao);
        listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
    }

    // Via pix
    public void depositar(String pixPessoal, BigDecimal deposito) {
        if (this.pix.equals(pixPessoal)) {
            saldo = this.saldo.add(deposito);
            String descricao = "DEPÓSITO: +" + deposito.toString();
            extrato.put(LocalDateTime.now(), descricao);
            Transacao transacao = new Transacao(LocalDateTime.now(), TipoTransacao.DEPOSITO, descricao);
            extrato2.add(transacao);
            listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
            return;
        }
        System.out.println("Pix incorreto para efetuar o depósito! Por favor, insira seu pix corretamente.");
    }

    // Via depósito bancário usual
    public void depositar(String agenciaPessoal, String contaPessoal, BigDecimal deposito) {
        if (this.agencia.equals(agenciaPessoal) && this.conta.equals(contaPessoal)) {
            saldo = this.saldo.add(deposito);
            String descricao = "DEPÓSITO: +" + deposito.toString();
            extrato.put(LocalDateTime.now(), descricao);
            Transacao transacao = new Transacao(LocalDateTime.now(), TipoTransacao.DEPOSITO, descricao);
            extrato2.add(transacao);
            listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
            return;
        }
        System.out.println("Dados inválidos para efetuar o depósito! Por favor, insira sua agência e conta bancária corretamente.");
    }

    // Método de visualização do extrato bancário
    public void verExtrato() {
        System.out.println("--- Extrato: ---");
        System.out.println("- DATA: - DESCRIÇÃO:");
        this.extrato.forEach((k, v) -> {
            if (!k.isAfter(LocalDateTime.now())) System.out.println(k + ": " + v);
        });
    }

    public void verExtrato(LocalDateTime dataInicialDeVarredura) {
        if (LocalDateTime.now().isBefore(dataInicialDeVarredura)) {
            System.out.println("Data inválida! Por favor, insira uma data anterior ou igual a data de hoje (" + LocalDateTime.now() + ").");
            return;
        }

        System.out.println("--- Extrato: ---");
        System.out.println("- DATA: - DESCRIÇÃO:");
        this.extrato.forEach((k, v) -> {
            if (!k.isBefore(dataInicialDeVarredura) && !k.isAfter(LocalDateTime.now())) System.out.println(k + ": " + v);
        });
    }

    // Pix entre contas correntes
    public void transferir(String pixDestinatario, BigDecimal valorTransferido) throws CloneNotSupportedException {
        if (!verificarPix(pixDestinatario)) {
            System.out.println("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
            return;
        }
        if (valorTransferido.compareTo(this.saldo) > 0) {
            System.out.println("Saldo insuficiente! Pix não autorizado.");
            return;
        }

        ContaCorrente corrente = localizarConta(pixDestinatario);

        String descricao = " => DE " + this.nomeTitular + "; PARA " + corrente.nomeTitular + ";";

        corrente.saldo = corrente.saldo.add(valorTransferido);
        corrente.extrato.put(LocalDateTime.now(), "Pix recebido +" + valorTransferido + descricao);
        Transacao transacao = new Transacao(LocalDateTime.now(), TipoTransacao.PIX, "Pix recebido +" + valorTransferido + descricao);
        corrente.extrato2.add(transacao.clone());
        listaClientes.set(listaClientes.indexOf(localizarConta(pixDestinatario)), corrente);

        saldo = this.saldo.subtract(valorTransferido);
        extrato.put(LocalDateTime.now(), "Pix feito -" + valorTransferido + descricao);
        transacao.setDescricao("Pix feito -" + valorTransferido + descricao);
        extrato2.add(transacao.clone());
        listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
    }

    // Transferência entre contas correntes
    public void transferir(String agenciaDestinatario, String contaDestinatario, BigDecimal valorTransferido) throws CloneNotSupportedException {
        if (!verificarAgenciaEConta(agenciaDestinatario, contaDestinatario)) {
            System.out.println("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
            return;
        }
        if (valorTransferido.compareTo(this.saldo) > 0) {
            System.out.println("Saldo insuficiente! Transferência não autorizada.");
            return;
        }

        ContaCorrente corrente = localizarConta(agenciaDestinatario, contaDestinatario);

        String descricao = " => DE " + this.nomeTitular + "; PARA " + corrente.nomeTitular + ";";

        corrente.saldo = corrente.saldo.add(valorTransferido);
        corrente.extrato.put(LocalDateTime.now(), "Transferência recebida: +" + valorTransferido + descricao);
        Transacao transacao = new Transacao(LocalDateTime.now(), TipoTransacao.TRANSFERENCIA, "Transferência recebida: +" + valorTransferido + descricao);
        corrente.extrato2.add(transacao.clone());
        listaClientes.set(listaClientes.indexOf(localizarConta(agenciaDestinatario, contaDestinatario)), corrente);

        saldo = this.saldo.subtract(valorTransferido);
        extrato.put(LocalDateTime.now(), "Transferência feita: -" + valorTransferido + descricao);
        transacao.setDescricao("Transferência feita: -" + valorTransferido + descricao);
        extrato2.add(transacao.clone());
        listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
    }

    // Pix programado entre contas correntes
    public void transferir(LocalDateTime dataAgendada, String pixDestinatario, BigDecimal valorTransferido)
            throws CloneNotSupportedException {
        if (!verificarPix(pixDestinatario)) {
            System.out.println("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
            return;
        }
        if (valorTransferido.compareTo(this.saldo) > 0) {
            System.out.println("Saldo insuficiente! Pix não autorizado.");
            return;
        }
        if (dataAgendada.isBefore(LocalDateTime.now())) {
            System.out.println("Data de agendamento inválida! Por favor, insira um agendamento válido.");
            return;
        }
        if (!dataAgendada.isAfter(LocalDateTime.now())) { // Apenas para validar o agendamento

            ContaCorrente corrente = localizarConta(pixDestinatario);

            String descricao = " => DE " + this.nomeTitular + "; PARA " + corrente.nomeTitular + ";";

            corrente.saldo = corrente.saldo.add(valorTransferido);
            corrente.extrato.put(dataAgendada, "Pix agendado +" + valorTransferido + descricao);
            Transacao transacao = new Transacao(dataAgendada, TipoTransacao.PIX, "Pix agendado +" + valorTransferido + descricao);
            corrente.extrato2.add(transacao.clone());
            listaClientes.set(listaClientes.indexOf(localizarConta(pixDestinatario)), corrente);

            saldo = this.saldo.subtract(valorTransferido);
            extrato.put(dataAgendada, "Pix agendado -" + valorTransferido + descricao);
            transacao.setDescricao("Pix agendado -" + valorTransferido + descricao);
            extrato2.add(transacao.clone());
            listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);

        }
    }

    // Transferência programada entre contas correntes
    public void transferir(LocalDateTime dataAgendada, String agenciaDestinatario, String contaDestinatario, BigDecimal valorTransferido)
            throws CloneNotSupportedException {
        if (!verificarAgenciaEConta(agenciaDestinatario, contaDestinatario)) {
            System.out.println("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
            return;
        }
        if (valorTransferido.compareTo(this.saldo) > 0) {
            System.out.println("Saldo insuficiente! Transferência não autorizada.");
            return;
        }
        if (dataAgendada.isBefore(LocalDateTime.now())) {
            System.out.println("Data de agendamento inválida! Por favor, insira um agendamento válido.");
            return;
        }
        if (!dataAgendada.isAfter(LocalDateTime.now())) { // Apenas para validar o agendamento

            ContaCorrente corrente = localizarConta(agenciaDestinatario, contaDestinatario);

            String descricao = " => DE " + this.nomeTitular + "; PARA " + corrente.nomeTitular + ";";

            corrente.saldo = corrente.saldo.add(valorTransferido);
            corrente.extrato.put(dataAgendada, "Transferência agendada: +" + valorTransferido + descricao);
            Transacao transacao = new Transacao(dataAgendada, TipoTransacao.TRANSFERENCIA, "Transferência agendada: +" + valorTransferido + descricao);
            corrente.extrato2.add(transacao.clone());
            listaClientes.set(listaClientes.indexOf(localizarConta(agenciaDestinatario, contaDestinatario)), corrente);

            saldo = this.saldo.subtract(valorTransferido);
            extrato.put(dataAgendada, "Transferência agendada: -" + valorTransferido + descricao);
            transacao.setDescricao("Transferência agendada: -" + valorTransferido + descricao);
            extrato2.add(transacao.clone());
            listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);

        }
    }

    public static boolean verificarPix(String pix) {
        return listaClientes.stream()
                        .map(ContaCorrente::getPix)
                        .collect(Collectors.toList())
                        .contains(pix);
    }

    public static boolean verificarAgenciaEConta(String agencia, String conta) {
        return listaClientes.stream()
                        .map(contaCorrente -> contaCorrente.agencia + " " + contaCorrente.conta)
                        .collect(Collectors.toList())
                        .contains(agencia + " " + conta);
    }

    public static ContaCorrente localizarConta(String pix) {
        return listaClientes.stream()
                            .filter(contaCorrente -> contaCorrente.pix.equals(pix))
                            .collect(Collectors.toList())
                            .get(0);
    }

    public static ContaCorrente localizarConta(String agencia, String conta) {
        return listaClientes.stream()
                            .filter(contaCorrente -> contaCorrente.agencia.equals(agencia) && contaCorrente.conta.equals(conta))
                            .collect(Collectors.toList())
                            .get(0);
    }
}
