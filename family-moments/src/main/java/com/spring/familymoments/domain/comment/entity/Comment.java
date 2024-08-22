package com.spring.familymoments.domain.comment.entity;

import com.spring.familymoments.domain.common.BaseEntity;
import com.spring.familymoments.domain.post.entity.Post;
import com.spring.familymoments.domain.user.entity.User;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "Comment")
@Getter
@NoArgsConstructor(force = true)
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentId", nullable = false, updatable = false)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post postId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "int unsigned")
    @ColumnDefault("0")
    private int reported;

    @Column(name = "countLove", columnDefinition = "int unsigned")
    @ColumnDefault("0")
    private int countLove;

    /**
     * 댓글 삭제 API 관련 메소드
     */
    public void updateStatus(Status status) {
        this.status = status;
    }

    /***
     * SET NULL -> 추후 변경 예정
     */
    public void updateWriter() {
        this.writer = null;
    }

    /**
     * 댓글 수정 API 관련 메소드
     */
    public void updateContent(String content) {
        this.content = content;
    }


    public void increaseCountLove() {
        this.countLove = countLove + 1;
    }

    public void decreaseCountLove() {
        this.countLove = countLove - 1;
    }

    /**
     * 댓글 신고 API 관련 메소드
     */
    public void updateReported(int reported) { this.reported = reported; }
}

