package com.example.hr.auth.controller;

import com.example.hr.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RbacTestController {

    @GetMapping("/admin/ping")
    public ApiResponse<String> adminPing() {
        return ApiResponse.success("admin ok", "OK");
    }

    @GetMapping("/manager/ping")
    public ApiResponse<String> managerPing() {
        return ApiResponse.success("manager ok", "OK");
    }

    @GetMapping("/employee/ping")
    public ApiResponse<String> employeePing() {
        return ApiResponse.success("employee ok", "OK");
    }
}
