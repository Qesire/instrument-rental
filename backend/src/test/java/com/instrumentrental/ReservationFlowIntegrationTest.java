package com.instrumentrental;

import com.instrumentrental.domain.enums.InstrumentStatus;
import com.instrumentrental.domain.enums.UserRole;
import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.*;
import com.instrumentrental.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class ReservationFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_instrument_rental")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private InstrumentRepository instrumentRepository;
    @Autowired private InstrumentModelRepository modelRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;

    @Test
    void shouldFindAvailableInstrumentForModel() {
        Warehouse warehouse = warehouseRepository.save(
                Warehouse.builder().name("测试仓库").build());

        Category category = categoryRepository.save(
                Category.builder().name("吉他").sortOrder(1).build());

        InstrumentModel model = modelRepository.save(InstrumentModel.builder()
                .name("Test Guitar").brand("TestBrand")
                .category(category).dailyRate(new BigDecimal("100")).build());

        User user = userRepository.save(User.builder()
                .nickname("测试用户").role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE).build());

        Instrument instrument = instrumentRepository.save(Instrument.builder()
                .serialNo("TEST001").barcode("BARCODE001")
                .model(model).warehouse(warehouse)
                .status(InstrumentStatus.IN_STOCK).build());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(3);

        assertThat(instrumentRepository.findAvailableForModel(model.getId(), start, end))
                .hasSize(1);

        Reservation reservation = Reservation.builder()
                .user(user).instrument(instrument)
                .startTime(start).endTime(end)
                .status(com.instrumentrental.domain.enums.ReservationStatus.RESERVED)
                .build();
        reservationRepository.save(reservation);

        assertThat(instrumentRepository.findAvailableForModel(model.getId(), start, end))
                .isEmpty();
    }

    @Test
    void shouldNotFindAvailableWhenOutOfStock() {
        Warehouse warehouse = warehouseRepository.save(
                Warehouse.builder().name("空仓库").build());
        Category category = categoryRepository.save(
                Category.builder().name("键盘").sortOrder(2).build());
        InstrumentModel model = modelRepository.save(InstrumentModel.builder()
                .name("Test Keyboard").category(category).build());

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(3);

        assertThat(instrumentRepository.findAvailableForModel(model.getId(), start, end))
                .isEmpty();
    }
}