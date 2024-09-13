package com.example.login_auth_api.services.reports;

import com.example.login_auth_api.domain.categories.Category;
import com.example.login_auth_api.domain.sales.PaymentMethod;
import com.example.login_auth_api.domain.sales.Sale;
import com.example.login_auth_api.repositories.SaleRepository; // Importe o repositório necessário
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ReportNumbersService {

    private final SaleRepository saleRepository;

    @Autowired
    public ReportNumbersService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    // Método para retornar o número de vendas por tipo de pagamento
    public Map<PaymentMethod, Integer> getSalesByPaymentMethod() {
        // Obtém a data e hora atual
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Obtém as vendas do mês atual
        List<Sale> salesThisMonth = saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth);

        // Cria um mapa para contar as vendas por método de pagamento
        Map<PaymentMethod, Integer> salesCount = new HashMap<>();

        // Inicializa o mapa com os métodos de pagamento
        for (PaymentMethod method : PaymentMethod.values()) {
            salesCount.put(method, 0);
        }

        // Conta as vendas por método de pagamento
        for (Sale sale : salesThisMonth) {
            PaymentMethod method = sale.getPaymentMethod(); // Assumindo que Sale tem um método getPaymentMethod() que retorna PaymentMethod
            salesCount.put(method, salesCount.getOrDefault(method, 0) + 1);
        }

        return salesCount;
    }

    // Método para contar vendas com isGift = true
    public long countByIsGiftTrueAndSaleDateBetween() {
        // Obtém o mês e ano atuais
        YearMonth currentMonth = YearMonth.now();
        // Define o primeiro segundo do primeiro dia do mês atual
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        // Define o último segundo do último dia do mês atual
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Chama o método do repositório com o intervalo de datas
        return saleRepository.countByIsGiftTrueAndSaleDateBetween(startDate, endDate);
    }
    // Método para contar vendas com isGift = false
    public long countByIsGiftFalseAndSaleDateBetween() {
        // Obtém o mês e ano atuais
        YearMonth currentMonth = YearMonth.now();
        // Define o primeiro segundo do primeiro dia do mês atual
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        // Define o último segundo do último dia do mês atual
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Chama o método do repositório com o intervalo de datas
        return saleRepository.countByIsGiftFalseAndSaleDateBetween(startDate, endDate);
    }


    //Métodos para contar vendas dia mes e ano
    // Método para contar vendas do dia
    public long getCountOfSalesToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return saleRepository.findBySaleDateBetween(startOfDay, endOfDay).size();
    }
    // Método para contar vendas do mês
    public long getCountOfSalesThisMonth() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(23, 59, 59);
        return saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth).size();
    }
    // Método para contar vendas do ano
    public long getCountOfSalesThisYear() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        LocalDateTime startOfYear = firstDayOfYear.atStartOfDay();
        LocalDateTime endOfYear = lastDayOfYear.atTime(23, 59, 59);
        return saleRepository.findBySaleDateBetween(startOfYear, endOfYear).size();
    }
    //metodo que pega as vendas do ultimos 6 meses
    public Map<String, Integer> getSalesByMonthLastSixMonths() {
        // Data atual e seis meses anteriores
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = now.minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0); // Início do intervalo de 6 meses
        LocalDateTime endDateTime = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_999); // Último dia do mês atual

        // Recupera todas as vendas no intervalo de tempo desejado
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDateTime, endDateTime);

        // Mapa para contagem de vendas por mês
        Map<String, Integer> salesByMonth = new LinkedHashMap<>();

        // Lista de meses na ordem desejada
        List<String> monthOrder = Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC");

        // Itera sobre os últimos 6 meses incluindo o mês atual
        LocalDateTime monthStartDateTime = startDateTime;
        while (!monthStartDateTime.isAfter(now)) {
            LocalDateTime monthEndDateTime = monthStartDateTime.toLocalDate().withDayOfMonth(monthStartDateTime.toLocalDate().lengthOfMonth()).atTime(23, 59, 59);

            // Conta as vendas para o mês específico
            LocalDateTime finalMonthStartDateTime = monthStartDateTime;
            long count = sales.stream()
                    .filter(sale -> {
                        LocalDateTime saleDate = sale.getSaleDate();
                        return !saleDate.isBefore(finalMonthStartDateTime) && !saleDate.isAfter(monthEndDateTime);
                    })
                    .count();

            // Adiciona a contagem ao mapa
            String monthKey = monthStartDateTime.getMonth().toString().substring(0, 3).toUpperCase();
            salesByMonth.put(monthKey, (int) count);

            // Avança para o próximo mês
            monthStartDateTime = monthStartDateTime.plusMonths(1);
        }

        // Reordena o mapa para seguir a ordem dos meses de janeiro a dezembro
        Map<String, Integer> sortedSalesByMonth = new LinkedHashMap<>();
        for (String month : monthOrder) {
            if (salesByMonth.containsKey(month)) {
                sortedSalesByMonth.put(month, salesByMonth.get(month));
            }
        }

        return sortedSalesByMonth;
    }
    
    //metodos para retornar valor vendido dia mes e ano o subtotal o valor com desconto
    //Método para contar valor vendido do dia
    public BigDecimal getTotalSalesToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1); // Último momento do dia

        return saleRepository.findBySaleDateBetween(startOfDay, endOfDay).stream()
                .map(Sale::getSubtotal) // Supondo que Sale tem um método getTotalValue()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    //Método para contar valor vendido do mes
    public BigDecimal getTotalSalesThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1); // Último momento do mês

        return saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth).stream()
                .map(Sale::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    //Método para contar valor vendido do ano
    public BigDecimal getTotalSalesThisYear() {
        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfYear = startOfYear.plusYears(1).minusNanos(1); // Último momento do ano

        return saleRepository.findBySaleDateBetween(startOfYear, endOfYear).stream()
                .map(Sale::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    //metodo para o valor vendido nos ultimos 6 meses
    public Map<String, BigDecimal> getTotalSalesByMonthLastSixMonths() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.withDayOfMonth(now.getDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime startDate = now.minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);

        // Agrupar as vendas por mês
        Map<Month, BigDecimal> salesByMonth = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getMonth(),
                        Collectors.mapping(Sale::getSubtotal, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Ordenar e preparar os meses do mais antigo para o mais recente
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i).withDayOfMonth(1);
            Month month = monthDate.getMonth();
            String monthName = month.toString().substring(0, 3).toUpperCase();
            result.put(monthName, salesByMonth.getOrDefault(month, BigDecimal.ZERO));
        }

        return result;
    }


    //metodos para categoria mais vendida
    // Método para obter o ranking de categorias mais vendidas do dia em termos de quantidade de itens
    public Map<String, Long> getTopCategoriesForToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Sale> sales = saleRepository.findBySaleDateBetween(startOfDay, endOfDay);

        // Agrupa as vendas por categoria e conta a quantidade de itens vendidos
        Map<Category, Long> categoryItemCount = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),  // Obtém a categoria do produto
                        Collectors.counting()  // Conta o número de itens por categoria
                ));

        // Ordena as categorias por número de itens vendidos
        Map<String, Long> sortedCategorySales = categoryItemCount.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getNome(),  // Assumindo que Category tem um método getNome()
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new  // Mantém a ordem de inserção
                ));

        return sortedCategorySales.isEmpty() ? Collections.singletonMap("Ainda não existe categoria mais vendida", 0L) : sortedCategorySales;
    }
    // Método para obter o ranking de categorias mais vendidas do mês em termos de quantidade de itens
    public Map<String, Long> getTopCategoriesForMonth() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = lastDayOfMonth.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth);

        // Agrupa as vendas por categoria e conta a quantidade de itens vendidos
        Map<Category, Long> categoryItemCount = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),  // Obtém a categoria do produto
                        Collectors.counting()  // Conta o número de itens por categoria
                ));

        // Ordena as categorias por número de itens vendidos
        Map<String, Long> sortedCategorySales = categoryItemCount.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getNome(),  // Assumindo que Category tem um método getNome()
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new  // Mantém a ordem de inserção
                ));

        return sortedCategorySales.isEmpty() ? Collections.singletonMap("Ainda não existe categoria mais vendida", 0L) : sortedCategorySales;
    }
    // Método para obter o ranking de categorias mais vendidas do ano em termos de quantidade de itens
    public Map<String, Long> getTopCategoriesForYear() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        LocalDate lastDayOfYear = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());

        LocalDateTime startOfYear = firstDayOfYear.atStartOfDay();
        LocalDateTime endOfYear = lastDayOfYear.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findBySaleDateBetween(startOfYear, endOfYear);

        // Agrupa as vendas por categoria e conta a quantidade de itens vendidos
        Map<Category, Long> categoryItemCount = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory(),  // Obtém a categoria do produto
                        Collectors.counting()  // Conta o número de itens por categoria
                ));

        // Ordena as categorias por número de itens vendidos
        Map<String, Long> sortedCategorySales = categoryItemCount.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getNome(),  // Assumindo que Category tem um método getNome()
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new  // Mantém a ordem de inserção
                ));

        return sortedCategorySales.isEmpty() ? Collections.singletonMap("Ainda não existe categoria mais vendida", 0L) : sortedCategorySales;
    }
}
