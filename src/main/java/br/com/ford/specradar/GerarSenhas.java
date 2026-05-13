package br.com.ford.specradar;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarSenhas {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String senhaAdmin    = "Admin@2026";
        String senhaAnalista = "Analyst@2026";

        System.out.println("admin hash:    " + encoder.encode(senhaAdmin));
        System.out.println("analista hash: " + encoder.encode(senhaAnalista));
    }
}