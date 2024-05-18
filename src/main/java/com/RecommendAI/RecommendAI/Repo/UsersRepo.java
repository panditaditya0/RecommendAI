package com.RecommendAI.RecommendAI.Repo;

import com.RecommendAI.RecommendAI.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UsersRepo extends JpaRepository<Users, Long> {

    @Query(value = "select * from users where mad_id = ?1", nativeQuery = true)
    public Users getUsingMadId(UUID uuid);


    @Query(value = "select * from users where user_id = ?1", nativeQuery = true)
    public Users getUsinguserId(UUID uuid);

    @Query(value = "UPDATE users\n" +
            "SET  sku_Ids= ?2\n" +
            "WHERE mad_id = ?1;", nativeQuery = true)
    public void saveByMadId(UUID madId, List<String> skus);
}
