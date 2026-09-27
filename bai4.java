// ==========================================
// 1. FILE CẤU HÌNH: application.yml
// ==========================================
/*
resilience4j:
  ratelimiter:
    instances:
      excelReportRateLimiter:
        limitForPeriod: 2
        limitRefreshPeriod: 60s
        timeoutDuration: 0s
  bulkhead:
    instances:
      excelReportBulkhead:
        maxConcurrentCalls: 5
        maxWaitDuration: 0s
*/

// ==========================================
// 2. FILE SOURCE CODE: ReportController.java
// ==========================================
package com.storex.report.controller;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @GetMapping("/excel")
    @RateLimiter(name = "excelReportRateLimiter", fallbackMethod = "rateLimiterFallback")
    @Bulkhead(name = "excelReportBulkhead", fallbackMethod = "bulkheadFallback")
    public ResponseEntity<String> downloadExcelReport() throws InterruptedException {
        // Giả lập thời gian xuất báo cáo Excel nặng
        Thread.sleep(3000);
        return ResponseEntity.ok("Tải báo cáo Excel thành công!");
    }

    // Fallback xử lý vi phạm RateLimiter -> Trả về HTTP 429 (Too Many Requests)
    public ResponseEntity<String> rateLimiterFallback(RequestNotPermitted ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Bạn thao tác quá nhanh, vui lòng thử lại sau");
    }

    // Fallback xử lý vi phạm Bulkhead -> Trả về HTTP 503 (Service Unavailable)
    public ResponseEntity<String> bulkheadFallback(BulkheadFullException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Server đang quá tải quá trình kết xuất");
    }
}