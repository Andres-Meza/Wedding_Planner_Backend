package ma.femme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ma.femme.entity.TableWedding;

public interface TableWeddingRepository extends JpaRepository<TableWedding, Long> {
}
