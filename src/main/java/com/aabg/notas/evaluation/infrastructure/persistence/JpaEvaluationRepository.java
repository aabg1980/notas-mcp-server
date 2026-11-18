package com.aabg.notas.evaluation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaEvaluationRepository extends JpaRepository<EvaluationEntity, Long> {

    List<EvaluationEntity> findByCategoryId(Long categoryId);

    List<EvaluationEntity> findByCategoryIdIn(List<Long> categoryIds);

    Optional<EvaluationEntity> findByCategoryIdAndName(Long categoryId, String name);
}
