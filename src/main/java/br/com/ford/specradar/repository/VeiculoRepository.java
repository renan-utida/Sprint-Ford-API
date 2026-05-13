package br.com.ford.specradar.repository;

import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findByAtivoTrue();

    List<Veiculo> findByMarca(MarcaVeiculo marca);

    List<Veiculo> findByMarcaAndAtivoTrue(MarcaVeiculo marca);
}