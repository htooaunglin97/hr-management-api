package com.example.hr;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(HrManagementApiApplication.class);

    @Test
    void verifyModularity() {
        modules.verify();
    }

    @Test
    void createDocumentation() {
        new Documenter(modules).writeModulesAsPlantUml();
    }
}