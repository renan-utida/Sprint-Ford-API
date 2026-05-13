package br.com.ford.specradar.repository;

import br.com.ford.specradar.domain.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByUsuarioId(Long usuarioId);

    List<Consulta> findByVeiculoId(Long veiculoId);
}