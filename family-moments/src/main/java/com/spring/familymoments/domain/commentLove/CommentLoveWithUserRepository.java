package com.spring.familymoments.domain.commentLove;

import com.spring.familymoments.domain.commentLove.entity.CommentLove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentLoveWithUserRepository extends JpaRepository<CommentLove, Long> {
    @Query("SELECT cl FROM CommentLove cl WHERE cl.userId.userId = :userId")
    List<CommentLove> findCommentLovesByUserId(@Param("userId") Long userId);
}
