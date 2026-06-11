package com.expensepro.repository;

import com.expensepro.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.isDefault=true OR c.user.id=:uid ORDER BY c.name")
    List<Category> findForUser(@Param("uid") Long userId);
}
