package com.resapori.e_commerce.repository;

import com.resapori.e_commerce.southbound.repository.IDashboardRepository;
import com.resapori.e_commerce.southbound.repository.dto.RawOrdersAggregationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DashboardRepositoryTest {

    @Autowired
    private IDashboardRepository dashboardRepository;

    @Test
    void testQueryExecution() {
        RawOrdersAggregationResult res = dashboardRepository.fetchOrdersAndRevenueAggregate(null, null, 7);
        assertNotNull(res);
        System.out.println("Orders res: " + res);
    }
}
