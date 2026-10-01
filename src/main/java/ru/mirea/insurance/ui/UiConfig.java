package ru.mirea.insurance.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.mirea.insurance.ui.console.ConsolePrinter;
import ru.mirea.insurance.ui.console.ConsoleReader;
import ru.mirea.insurance.ui.console.TablePrinter;

@Configuration
public class UiConfig {

    @Bean
    ConsolePrinter consolePrinter() {
        return ConsolePrinter.system();
    }

    @Bean
    TablePrinter tablePrinter(ConsolePrinter printer) {
        return new TablePrinter(printer);
    }

    @Bean
    ConsoleReader consoleReader(ConsolePrinter printer) {
        return new ConsoleReader(System.in, printer);
    }
}
