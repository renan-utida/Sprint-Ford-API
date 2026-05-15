-- Atualiza constraint de marca para incluir FORD
ALTER TABLE ford_veiculos
DROP CONSTRAINT chk_veiculos_marca;

ALTER TABLE ford_veiculos
    ADD CONSTRAINT chk_veiculos_marca CHECK (marca IN (
                                                       'FORD', 'TOYOTA', 'VOLKSWAGEN', 'CHEVROLET', 'FIAT',
                                                       'HYUNDAI', 'NISSAN', 'MITSUBISHI', 'JEEP', 'RAM', 'MERCEDES'
        ));

-- Ford Ranger Raptor 2025
INSERT INTO ford_veiculos (marca, modelo, versao, ano, ativo, criado_em)
VALUES ('FORD', 'Ranger', 'Raptor', 2025, 1, CURRENT_TIMESTAMP);

-- Especificacoes Ranger Raptor
-- O veiculo recebera o proximo ID disponivel apos os 3 do V5
-- Em H2 e Oracle com IDENTITY, sera o ID 4

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Motor', 'V6 3.0L Nano bi turbo', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Potencia', '397', 'cv', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Torque', '583', 'Nm', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Transmissao', 'AT de 10 velocidades e paddle shifters', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Tracao', '4WD', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Amortecedores', 'Live Valve FOX Racing 2.5"', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, '0-100 km/h', '5.8', 's', 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Modos de Conducao', 'Normal, Sport, Escorregadio, Lama, Areia, Rock Crawl, Baja', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Modos de Volante', 'Normal, Sport e Conforto', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Modos de Escapamento', 'Normal, Silencioso, Sport e Baja', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Modos de Amortecedor', 'Normal, Sport e Baja', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Farois', 'Matrix LED', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Rodas e Pneus', '17" com 285/70 R17 AT', NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO ford_especificacoes (veiculo_id, atributo, valor, unidade, disponivel, criado_em)
VALUES (4, 'Preco', '499000', 'R$', 1, CURRENT_TIMESTAMP);

COMMIT;