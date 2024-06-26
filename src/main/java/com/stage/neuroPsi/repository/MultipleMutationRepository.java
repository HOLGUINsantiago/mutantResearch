package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.stage.neuroPsi.models.MultipleMutation;

@Repository
public interface MultipleMutationRepository extends JpaRepository<MultipleMutation, String> {

}
