package com.expensepro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseProApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseProApplication.class, args);
        System.out.println("""
            ╔══════════════════════════════════════╗
            ║   ExpensePro API started on :8080    ║
            ╚══════════════════════════════════════╝
            """);
    }
}
