package com.aabg.notas.infrastructure.persistence.repository;

import com.aabg.notas.infrastructure.persistence.entity.RubricCriterionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaRubricCriterionRepository extends JpaRepository<RubricCriterionEntity, Long> {

    List<RubricCriterionEntity> findByEvaluationId(Long evaluationId);
}
