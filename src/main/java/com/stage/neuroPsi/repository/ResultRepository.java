package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stage.neuroPsi.models.AlignResults;

@Repository
public interface ResultRepository extends JpaRepository<AlignResults, Long> {
        @Modifying
        @Transactional
        @Query(value = "INSERT INTO align_results (alignment_length, e_value, line_end, line_start, line_id, nom, identity, read_id, wt, ref_end, ref_start, gaps, mismatchs) "
                        +
                        "VALUES (:alignmentLength, :eValue, :lineEnd, :lineStart, :lineId, :nom, :identity, :readId, :wt, :refEnd, :refStart, :gaps, :mismatchs)", nativeQuery = true)
        void insertAlignResults(@Param("alignmentLength") int alignmentLength,
                        @Param("eValue") String eValue,
                        @Param("lineEnd") int lineEnd,
                        @Param("lineStart") int lineStart,
                        @Param("lineId") String lineId,
                        @Param("nom") String nom,
                        @Param("identity") double identity,
                        @Param("readId") String readId,
                        @Param("wt") boolean wt,
                        @Param("refEnd") int refEnd,
                        @Param("refStart") int refStart,
                        @Param("gaps") int gaps,
                        @Param("mismatchs") int mismatchs);
}