package com.steel.perf;

import com.steel.perf.service.MaskingService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskingServiceTest {

    private final MaskingService svc = new MaskingService();

    @Test
    void authorizedRolesSeeAmount() {
        assertTrue(svc.canViewSalary(Set.of("HR")));
        assertEquals("20835.0", svc.maskAmount(20835d, Set.of("绩效专员")));
    }

    @Test
    void unauthorizedRolesMasked() {
        assertFalse(svc.canViewSalary(Set.of("GUEST", "SALES")));
        assertEquals("****", svc.maskAmount(20835d, Set.of("GUEST")));
        assertEquals("****", svc.maskAmount(20835d, null));
    }
}
