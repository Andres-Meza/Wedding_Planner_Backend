package ma.femme.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ma.femme.dto.FunAnswerCount;
import ma.femme.entity.FunQuizAnswer;

public interface FunQuizAnswerRepository extends JpaRepository<FunQuizAnswer, Long> {

  List<FunQuizAnswer> findByQuizId(Long quizId);

  Optional<FunQuizAnswer> findByQuizIdAndQuestionKey(Long quizId, String questionKey);

  List<FunQuizAnswer> findByQuizIdIn(Collection<Long> quizIds);

  // Distribución de respuestas: cuántas veces se eligió cada opción.
  @Query("""
      select new ma.femme.dto.FunAnswerCount(a.questionKey, a.optionKey, count(a))
      from FunQuizAnswer a
      group by a.questionKey, a.optionKey
      """)
  List<FunAnswerCount> countByQuestionAndOption();
}
