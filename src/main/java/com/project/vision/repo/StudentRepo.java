package com.project.vision.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

import com.project.vision.entity.Student;

public interface StudentRepo extends JpaRepository<Student, String> {

    @EntityGraph(attributePaths = "admin")
    @Query("select s from Student s where s.sap_id = :sapId")
    Optional<Student> findWithAdminBySapId(@Param("sapId") String sapId);

    long countByAdmin_Id(String adminId);

    @Query("select case when count(s) > 0 then true else false end "
            + "from Student s where s.sap_id = :sapId and s.admin.id = :adminId")
    boolean existsBySapIdAndAdminId(@Param("sapId") String sapId, @Param("adminId") String adminId);

}
