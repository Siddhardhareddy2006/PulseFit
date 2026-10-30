package com.pulsefit.memberservice.repo;
import com.pulsefit.memberservice.entity.Member; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface MemberRepository extends JpaRepository<Member,Long>{List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String a,String b);}
