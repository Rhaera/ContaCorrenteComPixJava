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
    private List<String> pix;
//    private Map<LocalDateTime, String> extrato;
    private List<Transacao> extrato; // Novo extrato
    @Getter
    private BigDecimal saldo;

    // Estrutura de dados estática para armazenar os clientes
    private static final List<ContaCorrente> listaClientes = new ArrayList<>();

    // Builder implementation
    public static class AccountBuilder {

        private final String agencia;
        private final String conta;
        private String nomeTitular = "";
        private List<String> pix = new ArrayList<>();
        private final BigDecimal saldo = BigDecimal.ZERO;
//        private final Map<LocalDateTime, String> extrato = new HashMap<>();
        private final List<Transacao> extrato = new ArrayList<>();

        public AccountBuilder(String agencia, String conta) {
            this.agencia = agencia;
            this.conta   = conta;
        }

        public AccountBuilder nomeTitular(String nome) {
            this.nomeTitular = nome;
            return this;
        }

        public AccountBuilder pix(List<String> pix) {
            this.pix = pix;
            return this;
        }

        public ContaCorrente build() {
            listaClientes.add(new ContaCorrente(this)); // -> Certo!
            return new ContaCorrente(this);
        }
    }

    private ContaCorrente(AccountBuilder aBuilder) {

        this.agencia     = aBuilder.agencia;
        this.conta       = aBuilder.conta;
        this.nomeTitular = aBuilder.nomeTitular;
        this.pix         = aBuilder.pix;
        this.saldo       = aBuilder.saldo;
//        this.extrato     = aBuilder.extrato;
        this.extrato    = aBuilder.extrato;
        // listaClientes.add(new ContaCorrente(aBuilder)); -> Errado!
    }

    // Diretamente no caixa eletrônico:
    public void sacar(BigDecimal saque) throws IllegalArgumentException {
        validarSaldoParaTransferencia(saque);
        registrarTransacao(new Transacao(LocalDateTime.now(), TipoTransacao.SAQUE, "SAQUE: -" + saque + ";"), saque);
    }

    // Diretamente no caixa eletrônico:
    public void depositar(BigDecimal deposito) throws IllegalArgumentException {
        registrarTransacao(new Transacao(LocalDateTime.now(), TipoTransacao.DEPOSITO,"DEPÓSITO: +" + deposito), deposito);
    }

    // Via pix
    public void depositar(String pixPessoal, BigDecimal deposito) throws IllegalArgumentException {
        if (this.pix.contains(pixPessoal)) {
            registrarTransacao(new Transacao(LocalDateTime.now(), TipoTransacao.DEPOSITO, "DEPÓSITO: +" + deposito), deposito);
            return;
        }
        throw new IllegalArgumentException("Pix incorreto para efetuar o depósito! Por favor, insira seu pix corretamente.");
    }

    // Via depósito bancário usual
    public void depositar(String agenciaPessoal, String contaPessoal, BigDecimal deposito) throws IllegalArgumentException {
        if (this.agencia.equals(agenciaPessoal) && this.conta.equals(contaPessoal)) {
            registrarTransacao(new Transacao(LocalDateTime.now(), TipoTransacao.DEPOSITO, "DEPÓSITO: +" + deposito), deposito);
            return;
        }
        throw new IllegalArgumentException(
                "Dados inválidos para efetuar o depósito! Por favor, insira sua agência e conta bancária corretamente.");
    }

    private void validarValorTransferido(BigDecimal valorTransferido) {
        if (valorTransferido.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Valor para movimentação inválido! Por favor, digite uma quantia válida (positiva).");
    }

    private void validarSaldoParaTransferencia(BigDecimal valorTransferido) {
        if (valorTransferido.compareTo(this.saldo) > 0)
            throw new IllegalArgumentException("Saldo insuficiente! Transferência não autorizada.");
    }

    private void registrarTransacao(Transacao transacao, BigDecimal valorTransferido) throws IllegalArgumentException {
        validarValorTransferido(valorTransferido);
        if (transacao.getTipoTransacao().equals(TipoTransacao.SAQUE)) saldo = this.saldo.subtract(valorTransferido);
        if (transacao.getTipoTransacao().equals(TipoTransacao.DEPOSITO)) saldo = this.saldo.add(valorTransferido);
        extrato.add(transacao);
        listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
    }

    private void registrarTransacao(Transacao transacao, BigDecimal valorTransferido, ContaCorrente contaDestinatario)
            throws IllegalArgumentException, CloneNotSupportedException {
        validarValorTransferido(valorTransferido);
        validarSaldoParaTransferencia(valorTransferido);
        if (transacao.getDataTransacao().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Data de agendamento inválida! Por favor, insira um agendamento válido.");
        if (!transacao.getDataTransacao().isAfter(LocalDateTime.now().plusSeconds(5L))) { // Apenas para validar o agendamento
            saldo = this.saldo.subtract(valorTransferido);
            contaDestinatario.saldo = contaDestinatario.saldo.add(valorTransferido);
            contaDestinatario.extrato.add(transacao.clone());
            listaClientes.set(listaClientes.indexOf(localizarConta(contaDestinatario.agencia, contaDestinatario.conta)), contaDestinatario);
        }
        transacao.setDescricao("Transferência " + validarDataParaTransferir(transacao) + ": -" + valorTransferido +
                " => DE " + this.nomeTitular + "; PARA " + contaDestinatario.nomeTitular + ";");
        extrato.add(transacao.clone());
        listaClientes.set(listaClientes.indexOf(localizarConta(this.agencia, this.conta)), this);
    }

    private String validarDataParaTransferir(Transacao transacao) {
        if (transacao.getDataTransacao().isBefore(LocalDateTime.now().plusSeconds(5L))) return "feita";
        return "agendada";
    }

    // Método de visualização do extrato bancário
    public void verExtrato() {
        System.out.println("---------- EXTRATO: ----------");
        System.out.println("- DATA:          - TIPO:         - DESCRIÇÃO:");

        this.extrato
                .stream()
                .map(Transacao::formatarParaExtrato)
                .forEach(System.out::println);

        /* versão com o extrato anterior (Map)
        this.extrato.forEach((k, v) -> {
            if (!k.isAfter(LocalDateTime.now())) System.out.println(k + ": " + v);
        }); */
    }

    public void verExtrato(LocalDateTime dataInicialDeVarredura) throws IllegalArgumentException {
        if (LocalDateTime.now().isBefore(dataInicialDeVarredura))
            throw new IllegalArgumentException(
                    "Data inválida! Por favor, insira uma data anterior ou igual a data de hoje (" + LocalDateTime.now() + ").");

        System.out.println("---------- EXTRATO: ----------");
        System.out.println("- DATA:          - TIPO:         - DESCRIÇÃO:");

        this.extrato
                .stream()
                .filter(transacao -> !transacao.getDataTransacao().isBefore(dataInicialDeVarredura) &&
                        !transacao.getDataTransacao().isAfter(LocalDateTime.now()))
                .map(Transacao::formatarParaExtrato)
                .forEach(System.out::println);

        /* versão com o extrato anterior (Map)
        System.out.println("--- Extrato: ---");
        System.out.println("- DATA: - DESCRIÇÃO:");
        this.extrato.forEach((k, v) -> {
            if (!k.isBefore(dataInicialDeVarredura) && !k.isAfter(LocalDateTime.now())) System.out.println(k + ": " + v);
        }); */
    }

    // Pix entre contas correntes
    public void transferir(String pixDestinatario, BigDecimal valorTransferido)
            throws CloneNotSupportedException, IllegalArgumentException {
        validarPix(pixDestinatario);
        registrarTransacao(new Transacao(LocalDateTime.now().plusSeconds(5L),
                        TipoTransacao.PIX,
                        formatarDescricaoParaTransferir(valorTransferido, localizarConta(pixDestinatario))),
                valorTransferido, localizarConta(pixDestinatario));
    }

    // Transferência entre contas correntes
    public void transferir(String agenciaDestinatario, String contaDestinatario, BigDecimal valorTransferido)
            throws CloneNotSupportedException, IllegalArgumentException {
        validarAgenciaEConta(agenciaDestinatario, contaDestinatario);
        registrarTransacao(new Transacao(LocalDateTime.now().plusSeconds(5L),
                        TipoTransacao.TRANSFERENCIA,
                        formatarDescricaoParaTransferir(valorTransferido, localizarConta(agenciaDestinatario, contaDestinatario))),
                valorTransferido, localizarConta(agenciaDestinatario, contaDestinatario));
    }

    // Pix programado entre contas correntes
    public void transferir(LocalDateTime dataAgendada, String pixDestinatario, BigDecimal valorTransferido)
            throws CloneNotSupportedException, IllegalArgumentException {
        validarPix(pixDestinatario);
        registrarTransacao(new Transacao(dataAgendada.plusSeconds(5L),
                        TipoTransacao.PIX,
                        formatarDescricaoParaTransferir(valorTransferido, localizarConta(pixDestinatario))),
                valorTransferido, localizarConta(pixDestinatario));
    }

    // Transferência programada entre contas correntes
    public void transferir(LocalDateTime dataAgendada, String agenciaDestinatario, String contaDestinatario, BigDecimal valorTransferido)
            throws CloneNotSupportedException, IllegalArgumentException {
        validarAgenciaEConta(agenciaDestinatario, contaDestinatario);
        registrarTransacao(new Transacao(dataAgendada.plusSeconds(5L),
                        TipoTransacao.TRANSFERENCIA,
                        formatarDescricaoParaTransferir(valorTransferido, localizarConta(agenciaDestinatario, contaDestinatario))),
                valorTransferido, localizarConta(agenciaDestinatario, contaDestinatario));
    }

    private String formatarDescricaoParaTransferir(BigDecimal valorTransferido, ContaCorrente contaDestinatario) {
        return "Transferência recebida: +" + valorTransferido + " => DE " + this.nomeTitular +
                "; PARA " + localizarConta(contaDestinatario.agencia, contaDestinatario.conta).nomeTitular + ";";
    }

    private static boolean verificarPix(String pixCadastrado) {
        // Método com stream
        return listaClientes.stream()
                .map(ContaCorrente::getPix)
                .map(chavesPix -> chavesPix.stream()
                        .reduce("", String::concat))
                .reduce("", String::concat)
                .contains(pixCadastrado);

        /* Método sem streams
        for (ContaCorrente contaCorrente: listaClientes) {

            if (contaCorrente.pix.contains(pixCadastrado)) return true;

        } return false; */
    }

    private static boolean verificarAgenciaEConta(String agenciaCadastrada, String contaCadastrada) {
        return listaClientes.stream()
                        .map(contaCorrente -> contaCorrente.agencia + contaCorrente.conta)
                        .collect(Collectors.toList())
                        .contains(agenciaCadastrada + contaCadastrada);
    }

    private static ContaCorrente localizarConta(String pixCadastrado) {
        return listaClientes.stream()
                            .filter(contaCorrente -> contaCorrente.pix.contains(pixCadastrado))
                            .collect(Collectors.toList())
                            .get(0);
    }

    private static ContaCorrente localizarConta(String agenciaCadastrada, String contaCadastrada) {
        return listaClientes.stream()
                            .filter(contaCorrente -> contaCorrente.agencia.equals(agenciaCadastrada) &&
                                    contaCorrente.conta.equals(contaCadastrada))
                            .collect(Collectors.toList())
                            .get(0);
    }

    private void validarPix(String pixDestinatario) {
        if (!verificarPix(pixDestinatario) || this.pix.contains(pixDestinatario))
            throw new IllegalArgumentException("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
    }

    private void validarAgenciaEConta(String agenciaDestinatario, String contaDestinatario) {
        if (!verificarAgenciaEConta(agenciaDestinatario, contaDestinatario) ||
                localizarConta(agenciaDestinatario, contaDestinatario).equals(this))
            throw new IllegalArgumentException("Dados bancários incorretos! Por favor, insira os dados de uma conta válida.");
    }

    public void adicionarPix(String novoPix) throws IllegalArgumentException {
        // Garante a unicidade de cada chave pix
        if (this.pix.contains(novoPix))
            throw new IllegalArgumentException("Alerta! Chave pix já cadastrada. Por favor, insira uma nova chave pix.");
        this.pix.add(novoPix);
    }
}
