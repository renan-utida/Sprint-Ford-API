package br.com.ford.specradar;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

// Roda todos os testes dentro dos pacotes indicados
@Suite
@SelectPackages("br.com.ford.specradar.service")
public class SuiteDeTestesGeral {
    // Nenhum código necessário aqui
}