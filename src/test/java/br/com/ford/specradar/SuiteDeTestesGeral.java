package br.com.ford.specradar;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

// Roda todos os testes dentro dos pacotes indicados
@Suite
@SelectPackages({
        "br.com.ford.specradar.service",
        "br.com.ford.specradar.domain",
        "br.com.ford.specradar.exception",
        "br.com.ford.specradar.security"
})
public class SuiteDeTestesGeral {
    // Nenhum código necessário aqui
}