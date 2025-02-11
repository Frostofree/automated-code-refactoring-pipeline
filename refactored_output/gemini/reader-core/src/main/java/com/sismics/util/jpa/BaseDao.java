```java
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.jpa.filter.FilterCriteria;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public abstract class BaseDao<T> implements JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    public PaginatedList<T> findByCriteria(C criteria, FilterCriteria filterCriteria, PaginatedList<T> list, SortCriteria sortCriteria) {
        Specification<T> specification = SpecificationFactory.createSpecification(criteria, filterCriteria);
        Pageable pageable = PageableFactory.createPageable(list, sortCriteria);
        Page<T> page = findAll(specification, pageable);
        list.setItems(page.getContent());
        list.setTotal(page.getTotalElements());
        return list;
    }

    public List<T> findByCriteria(C criteria, FilterCriteria filterCriteria, SortCriteria sortCriteria) {
        Specification<T> specification = SpecificationFactory.createSpecification(criteria, filterCriteria);
        Sort sort = SortFactory.createSort(sortCriteria);
        return findAll(specification, sort);
    }

    public List<T> findByCriteria(C criteria, FilterCriteria filterCriteria) {
        Specification<T> specification = SpecificationFactory.createSpecification(criteria, filterCriteria);
        return findAll(specification);
    }

    public Optional<T> findFirstByCriteria(C criteria, FilterCriteria filterCriteria) {
        Example<T> example = ExampleFactory.createExample(criteria);
        Specification<T> specification = SpecificationFactory.createSpecification(filterCriteria);
        return findOne(Specification.where(example).and(specification));
    }

    protected abstract Example<T> createExample(C criteria);
    
    protected abstract Specification<T> createSpecification(FilterCriteria filterCriteria);
}
```