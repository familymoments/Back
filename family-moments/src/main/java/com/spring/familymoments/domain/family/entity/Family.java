package com.spring.familymoments.domain.family.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring.familymoments.config.BaseException;
import com.spring.familymoments.domain.common.BaseEntity;
import com.spring.familymoments.domain.common.entity.UserFamily;
import com.spring.familymoments.domain.family.model.FamilyRes;
import com.spring.familymoments.domain.family.model.MyFamilyRes;
import com.spring.familymoments.domain.user.entity.User;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "Family")
@Getter
@ToString
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class Family extends BaseEntity {


    @Id
    @Column(name = "familyId", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

    @Column(nullable = false, length = 20)
    private String familyName;

    @Column(columnDefinition = "int unsigned")
    private int uploadCycle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inviteCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String representImg;

    @OneToMany(mappedBy = "familyId")
    private List<UserFamily> userFamilies = new ArrayList<>();

    public Family(Long familyId) {
        this.familyId = familyId;
    }

    public FamilyRes toFamilyRes(){
        return FamilyRes.builder()
                .familyId(familyId)
                .owner(owner.getNickname())
                .familyName(familyName)
                .uploadCycle(uploadCycle)
                .inviteCode(inviteCode)
                .representImg(representImg)
                .build();
    }

    public MyFamilyRes toMyFamilyRes(){
        return MyFamilyRes.builder()
                .familyId(familyId)
                .familyName(familyName)
                .representImg(representImg)
                .build();
    }


    /**
     * 가족 삭제 API 관련 메소드
     */
    public void updateStatus(Status status) {
        this.status = status;
    }

    /**
     * 가족 업로드 주기 수정 API 관련 메소드
     */
    public void updateUploadCycle(int uploadCycle) {
        this.uploadCycle = uploadCycle;
    }

    public void updateFamily(String familyName, String representImg){
        this.familyName = familyName;
        this.representImg = representImg;
    }

    /**
     * 가족 권한 수정 API 관련 메소드
     */
    public void updateFamilyOwner(User owner) {
        this.owner = owner;
    }

    public boolean isOwner(User user){
        return user.equals(owner);
    }
}

