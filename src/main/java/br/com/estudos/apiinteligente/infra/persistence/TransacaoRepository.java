package br.com.estudos.apiinteligente.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findAllByOrderByDataDescIdDesc();

    List<Transacao> findByDataBetweenOrderByDataDesc(LocalDate inicio, LocalDate fim);
}
