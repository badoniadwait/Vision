package com.project.vision.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.vision.entity.Student;

public interface StudentRepo extends JpaRepository<Student, String> {

    @Query("select case when count(s) > 0 then true else false end "
            + "from Student s where s.sap_id = :sapId and s.admin.id = :adminId")
    boolean existsBySapIdAndAdminId(@Param("sapId") String sapId, @Param("adminId") String adminId);

}
