package repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import model.Element;

public interface ElementRepository extends JpaRepository<Element, Long> {
	List<Element> findByTitle(String title);
	Optional<Element> findById(long id);
}
