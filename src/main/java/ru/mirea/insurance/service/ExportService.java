package ru.mirea.insurance.service;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.util.ExcelExporter;

/** Экспорт отчётов: сервис собирает строки, ExcelExporter пишет файл. */
@Service
public class ExportService {
    public static final String DEFAULT_FILE = "policies.xlsx";

    private final PolicyService policyService;
    private final ExcelExporter excelExporter;

    public ExportService(PolicyService policyService, ExcelExporter excelExporter) {
        this.policyService = policyService;
        this.excelExporter = excelExporter;
    }

    /** @return путь к созданному .xlsx */
    public Path exportPolicies(String fileName) {
        String name = fileName == null || fileName.isBlank() ? DEFAULT_FILE : fileName.trim();
        if (!name.endsWith(".xlsx")) {
            name = name + ".xlsx";
        }
        return excelExporter.exportPolicies(policyService.report(), Path.of(name));
    }
}
