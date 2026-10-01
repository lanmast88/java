package ru.mirea.insurance.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import ru.mirea.insurance.exception.DataAccessException;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.service.PolicyReportRow;

/** Выгрузка полисов в .xlsx на Apache POI. Данные готовит сервис, здесь только запись в файл. */
@Component
public class ExcelExporter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    private static final String[] HEADERS = {
            "ID", "Номер полиса", "Страхователь", "Вид страхования", "Статус",
            "Страховая сумма", "Премия", "Начало", "Окончание"};

    /** @return абсолютный путь к созданному файлу */
    public Path exportPolicies(List<PolicyReportRow> rows, Path file) {
        // try-with-resources: и книга, и поток закрываются сами, даже при исключении.
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file.toFile())) {
            Sheet sheet = workbook.createSheet("Полисы");

            CellStyle headerStyle = workbook.createCellStyle();
            Font bold = workbook.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("# ##0.00"));

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNumber = 1;
            for (PolicyReportRow row : rows) {
                Policy policy = row.policy();
                Row sheetRow = sheet.createRow(rowNumber++);
                sheetRow.createCell(0).setCellValue(policy.getId());
                sheetRow.createCell(1).setCellValue(policy.getNumber());
                sheetRow.createCell(2).setCellValue(row.clientName());
                sheetRow.createCell(3).setCellValue(policy.getType().getTitle());
                sheetRow.createCell(4).setCellValue(policy.getStatus().getTitle());
                money(sheetRow.createCell(5), policy.getInsuredSum(), moneyStyle);
                money(sheetRow.createCell(6), policy.getPremium(), moneyStyle);
                sheetRow.createCell(7).setCellValue(DATE.format(policy.getStartDate()));
                sheetRow.createCell(8).setCellValue(DATE.format(policy.getEndDate()));
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return file.toAbsolutePath();
        } catch (IOException e) {
            throw new DataAccessException("Не удалось записать файл " + file.toAbsolutePath(), e);
        }
    }

    /** Суммы пишем числом, а не строкой, иначе Excel не посчитает по ним итоги. */
    private void money(Cell cell, BigDecimal amount, CellStyle style) {
        cell.setCellValue(amount.doubleValue());
        cell.setCellStyle(style);
    }
}
