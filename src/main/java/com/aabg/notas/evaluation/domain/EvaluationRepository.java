package com.aabg.notas.evaluation.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Evaluation. La capa de dominio define el contrato;
 * la infraestructura lo implementa con SQLite + Spring Data JPA.
 */
public interface EvaluationRepository {

    /** Persiste el agregado completo (raíz, rúbrica y notas) y devuelve la versión con identidad asignada. */
    Evaluation save(Evaluation evaluation);

    Optional<Evaluation> findById(Long id);

    List<Evaluation> findByCategoryId(Long categoryId);

    /** Consulta todas las evaluaciones de una asignatura (a través de sus rubros). */
    List<Evaluation> findByCourseId(Long courseId);

    List<Evaluation> findAll();

    void deleteById(Long id);
}
