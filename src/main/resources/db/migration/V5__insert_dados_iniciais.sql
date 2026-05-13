-- ADMIN padrao
-- senha em texto: admin@specradar123
-- hash BCrypt gerado com strength 10
INSERT INTO ford_usuarios (nome, email, senha, role, ativo, criado_em)
VALUES (
           'Administrador SpecRadar',
           'admin@specradar.com',
           '$2a$12$/NBQZ.FDzpEl1JncunAFheIWUNTrLnuJKwCrjhmRfxGt.GoKWI8N6',
           'ADMIN',
           1,
           CURRENT_TIMESTAMP
       );

-- Analista padrao
-- senha em texto: analista@specradar123
INSERT INTO ford_usuarios (nome, email, senha, role, ativo, criado_em)
VALUES (
           'Analista Ford',
           'analista@specradar.com',
           '$2a$12$dIABQaCN37nBXPYvM93DuuJPaitcl1TxhBcSFC75HmGL77kzCCLu6',
           'ANALISTA',
           1,
           CURRENT_TIMESTAMP
       );

-- Toyota Hilux GR-Sport 2025
INSERT INTO ford_veiculos (marca, modelo, versao, ano, ativo, criado_em)
VALUES ('TOYOTA', 'Hilux', 'GR-Sport', 2025, 1, CURRENT_TIMESTAMP);

-- Volkswagen Amarok V6 2025
INSERT INTO ford_veiculos (marca, modelo, versao, ano, ativo, criado_em)
VALUES ('VOLKSWAGEN', 'Amarok', 'V6 Extreme', 2025, 1, CURRENT_TIMESTAMP);

-- Chevrolet S10 High Country 2025
INSERT INTO ford_veiculos (marca, modelo, versao, ano, ativo, criado_em)
VALUES ('CHEVROLET', 'S10', 'High Country', 2025, 1, CURRENT_TIMESTAMP);

-- Especificacoes Toyota Hilux (veiculo_id = 1)
INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (1, 'Motor', '2.8 Turbo Diesel', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (1, 'Potencia', '204', 'cv', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (1, 'Torque', '500', 'Nm', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (1, 'Preco', '379900', 'R$', 1, CURRENT_TIMESTAMP);

-- Especificacoes Amarok (veiculo_id = 2)
INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (2, 'Motor', '3.0 V6 TDI', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (2, 'Potencia', '258', 'cv', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (2, 'Torque', '600', 'Nm', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (2, 'Preco', '449900', 'R$', 1, CURRENT_TIMESTAMP);

COMMIT;