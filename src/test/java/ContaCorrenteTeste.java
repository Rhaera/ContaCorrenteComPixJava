import org.example.ContaCorrente;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ContaCorrenteTeste {

    // Testes com JUnit

    public ContaCorrente contaPaulo = new ContaCorrente(
        "Paulo",
            "0001",
            "00000-1",
            "paulo@email.com",
            new HashMap<>(),
            BigDecimal.ZERO
    );

    public ContaCorrente contaPedro = new ContaCorrente(
            "Pedro",
            "0002",
            "00000-2",
            "pedro@email.com",
            new HashMap<>(),
            BigDecimal.ZERO
    );

    @Test
    void realizarDepositoESaqueNasContas() {
        contaPaulo.depositar("pedro@email.com", BigDecimal.valueOf(20.00));
        contaPaulo.sacar(BigDecimal.valueOf(24.80));
        contaPaulo.depositar("0001", "00000-1", BigDecimal.valueOf(22.80));
        contaPedro.depositar("pedro@email.com", BigDecimal.valueOf(10.50));
        contaPedro.depositar(BigDecimal.valueOf(9.20));
        contaPedro.sacar(BigDecimal.valueOf(1.50));

        assertEquals(22.80d, contaPaulo.getSaldo().doubleValue());
        assertEquals(BigDecimal.valueOf(18.2), contaPedro.getSaldo());
    }

    @Test
    void realizarPixETransferenciaEntreContas() {
        realizarDepositoESaqueNasContas();

        contaPaulo.transferir(contaPedro, contaPaulo.getPix(), BigDecimal.valueOf(20)); // Errado
        contaPedro.transferir(contaPaulo, contaPaulo.getPix(), BigDecimal.valueOf(20L)); // Errado
        contaPedro.transferir(contaPaulo, contaPaulo.getPix(), BigDecimal.valueOf(10d)); // Certo
        contaPaulo.transferir(contaPedro, contaPaulo.getAgencia(), contaPedro.getConta(), BigDecimal.valueOf(1.05f)); // Errado
        contaPaulo.transferir(contaPedro, contaPedro.getAgencia(), contaPedro.getConta(), BigDecimal.valueOf(10)); // Certo
        contaPaulo.transferir(LocalDateTime.of(2020, 1, 29, 20, 2, 0), contaPedro, contaPedro.getPix(), BigDecimal.valueOf(10f)); // Errado
        contaPaulo.transferir(LocalDateTime.of(2024, 1, 29, 20, 2, 0), contaPedro, contaPedro.getPix(), BigDecimal.valueOf(10L)); // Certo
        contaPedro.transferir(LocalDateTime.of(2020, 1, 29, 20, 2, 0), contaPedro, contaPaulo.getPix(), BigDecimal.valueOf(10f)); // Errado
        contaPedro.transferir(LocalDateTime.of(2023, 3, 29, 0, 1, 10), contaPaulo, contaPaulo.getAgencia(), contaPaulo.getConta(), BigDecimal.valueOf(6.00)); // Certo

        assertEquals(18.80, contaPaulo.getSaldo().doubleValue());
        assertEquals(22.2f, contaPedro.getSaldo().floatValue());
    }

    @Test
    void exibirExtratoDasContas() {
        realizarPixETransferenciaEntreContas();

        System.out.println("--------------------------------------------------------");

        assertDoesNotThrow(() -> contaPaulo.verExtrato());

        System.out.println("--------------------------------------------------------");

        assertDoesNotThrow(() -> contaPedro.verExtrato(LocalDateTime.of(2023, 3, 18, 2, 8, 10)));

        System.out.println("--------------------------------------------------------");

        assertDoesNotThrow(() -> contaPedro.verExtrato(LocalDateTime.of(2023, 3, 16, 2, 8, 10)));

        System.out.println("--------------------------------------------------------");
    }
}
