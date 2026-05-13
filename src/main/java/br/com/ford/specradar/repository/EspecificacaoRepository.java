package br.com.ford.specradar.repository;

import br.com.ford.specradar.domain.Especificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspecificacaoRepository extends JpaRepository<Especificacao, Long> {

    List<Especificacao> findByVeiculoId(Long veiculoId);

    List<Especificacao> findByVeiculoIdAndDisponivelTrue(Long veiculoId);
}