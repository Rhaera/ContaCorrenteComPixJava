import org.example.ContaCorrente;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

// Testes com JUnit
public class ContaCorrenteTeste {

    // Builder Refactoring
    // List<String> chavesPixDePaulo = new ArrayList<>();
    public ContaCorrente contaPaulo = new ContaCorrente.AccountBuilder("0001", "00000-1")
                                                        .nomeTitular("Paulo")
                                                        .pix(new ArrayList<>())
                                                        .build();
/*
            new ContaCorrente(
            "Paulo",
            "0001",
            "00000-1",
            "paulo@email.com",
            new HashMap<>(),
            BigDecimal.ZERO
    );
*/

    // List<String> chavesPixDePedro = new ArrayList<>();
    public ContaCorrente contaPedro = new ContaCorrente.AccountBuilder("0002", "00000-2")
                                                        .nomeTitular("Pedro")
                                                        .pix(new ArrayList<>())
                                                        .build();
/*
            new ContaCorrente(
            "Pedro",
            "0002",
            "00000-2",
            "pedro@email.com",
            new HashMap<>(),
            BigDecimal.ZERO
    );
*/
    @Test
    void testarInstanciasENovasChavesPix() {
        contaPaulo.adicionarPix("paulo@email.com");
        contaPaulo.adicionarPix("000.000.000-01");
        System.out.println(contaPaulo);
        assertInstanceOf(ContaCorrente.class, contaPaulo);
        assertEquals(2, contaPaulo.getPix().size());
        contaPedro.adicionarPix("pedro@email.com");
        contaPedro.adicionarPix("000.000.000-02");
        System.out.println(contaPedro);
        assertInstanceOf(ContaCorrente.class, contaPedro);
        assertEquals(2, contaPedro.getPix().size());
    }

    @Test
    void realizarDepositoESaqueNasContas() throws IllegalArgumentException {
        testarInstanciasENovasChavesPix();

        assertThrows(IllegalArgumentException.class, () -> contaPaulo.depositar("pedro@email.com", BigDecimal.valueOf(20.00)));
        assertThrows(IllegalArgumentException.class, () -> contaPaulo.sacar(BigDecimal.valueOf(24.80)));

        contaPaulo.depositar("0001", "00000-1", BigDecimal.valueOf(22.80));
        contaPedro.depositar("pedro@email.com", BigDecimal.valueOf(10.50));
        contaPedro.depositar(BigDecimal.valueOf(9.20));
        contaPedro.sacar(BigDecimal.valueOf(1.50));

        assertEquals(22.80d, contaPaulo.getSaldo().doubleValue());
        assertEquals(BigDecimal.valueOf(18.2), contaPedro.getSaldo());
    }

    @Test
    void realizarPixETransferenciaEntreContas() throws CloneNotSupportedException, IllegalArgumentException {
        realizarDepositoESaqueNasContas();

        assertThrows(IllegalArgumentException.class, () -> contaPaulo.transferir(contaPaulo.getPix().get(0), BigDecimal.valueOf(20))); // Errado
        assertThrows(IllegalArgumentException.class, () -> contaPedro.transferir(contaPaulo.getPix().get(0), BigDecimal.valueOf(20L))); // Errado
        contaPedro.transferir(contaPaulo.getPix().get(1), BigDecimal.valueOf(10d)); // Certo
        assertThrows(IllegalArgumentException.class,
                () -> contaPaulo.transferir("0001", "00000-2", BigDecimal.valueOf(1.05f))); // Errado
        contaPaulo.transferir("0002", "00000-2", BigDecimal.valueOf(10)); // Certo
        assertThrows(IllegalArgumentException.class,
                () -> contaPaulo.transferir(LocalDateTime.of(2020, 1, 29, 20, 2, 0),
                contaPedro.getPix().get(1),
                BigDecimal.valueOf(10f))); // Errado
        contaPaulo.transferir(LocalDateTime.of(2024, 1, 29, 20, 2, 0),
                contaPedro.getPix().get(1),
                BigDecimal.valueOf(10L)); // Certo
        assertThrows(IllegalArgumentException.class,
                () -> contaPedro.transferir(LocalDateTime.of(2020, 1, 29, 20, 2, 0),
                contaPaulo.getPix().get(0),
                BigDecimal.valueOf(10f))); // Errado
        assertThrows(IllegalArgumentException.class, () -> contaPedro.transferir(LocalDateTime.of(2023, 3, 29, 0, 1, 10),
                "0001",
                "00000-1",
                BigDecimal.valueOf(6.00))); // Certo

        assertEquals(22.80, contaPaulo.getSaldo().doubleValue());
        assertEquals(18.2f, contaPedro.getSaldo().floatValue());
    }

    @Test
    void exibirExtratoDasContas() throws CloneNotSupportedException, IllegalArgumentException {
        realizarPixETransferenciaEntreContas();

        System.out.println("--------------------------------------------------------");

        assertDoesNotThrow(() -> contaPaulo.verExtrato());

        System.out.println("--------------------------------------------------------");

        assertDoesNotThrow(() -> contaPedro.verExtrato(LocalDateTime.of(2023, 3, 26, 2, 8, 10)));

        System.out.println("--------------------------------------------------------");

        assertDoesNotThrow(() -> contaPedro.verExtrato(LocalDateTime.of(2023, 3, 16, 2, 8, 10)));

        System.out.println("--------------------------------------------------------");
    }
}
