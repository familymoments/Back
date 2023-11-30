package com.spring.familymoments.domain.family;

import com.spring.familymoments.config.BaseException;
import com.spring.familymoments.config.BaseResponse;
import com.spring.familymoments.config.NoAuthCheck;
import com.spring.familymoments.config.secret.jwt.JwtService;
import com.spring.familymoments.domain.awsS3.AwsS3Service;
import com.spring.familymoments.domain.family.model.*;
import com.spring.familymoments.domain.user.AuthService;
import com.spring.familymoments.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.spring.familymoments.config.BaseResponseStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/families")
@Tag(name = "Family", description = "가족 API Document")
public class FamilyController {

    private final FamilyService familyService;
    private final JwtService jwtService;
    private final AuthService authService;
    @Autowired
    private final AwsS3Service awsS3Service;


    /**
     * 가족 생성 API
     * [POST] /family/:familyId
     * @return BaseResponse<PostFamilyRes>
     */
    @ResponseBody
    @NoAuthCheck
    @PostMapping("/family")
    @Operation(summary = "가족 생성", description = "가족 그룹을 생성합니다.")
    public BaseResponse<PostFamilyRes> createFamily(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "representImg") MultipartFile representImg,
            @RequestPart PostFamilyReq postFamilyReq) {
        try{
            String fileUrl = awsS3Service.uploadImage(representImg);        // 대표 이미지 넣기
            PostFamilyRes postFamilyRes = familyService.createFamily(user, postFamilyReq, fileUrl);
            return new BaseResponse<>(postFamilyRes);
        }catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 가족 정보 조회 API
     * [GET] /familyId
     * @return BaseResponse<FamilyDto>
     */
    @ResponseBody
    @GetMapping("/{familyId}")
    public BaseResponse<FamilyDto> getFamily(@PathVariable Long familyId,
                                             @AuthenticationPrincipal User user,
                                             @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){
        //return new BaseResponse<>(familyService.getFamily(familyId));
        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            FamilyDto familyDto = familyService.getFamily(familyId);
            return new BaseResponse<>(familyDto);
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(FIND_FAIL_FAMILY);
        }
    }

    /**
     * 닉네임 및 가족 생성일 조회 API
     * [GET] /:familyId/created
     * @return BaseResponse<FamilyDto>
     */
    @ResponseBody
    @NoAuthCheck
    @GetMapping("/{familyId}/created")
    @Operation(summary = "닉네임 및 가족 생성일 조회", description = "메인 페이지의 닉네임 및 가족 생성일 정보를 조회합니다.")
    public BaseResponse<GetFamilyCreatedNicknameRes> getFamilyCreatedNickname(
            @AuthenticationPrincipal User user,
            @PathVariable Long familyId) {
        try {
            GetFamilyCreatedNicknameRes getFamilyCreatedNicknameRes = familyService.getFamilyCreatedNickname(user, familyId);
            return new BaseResponse<>(getFamilyCreatedNicknameRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 가족원 전체 조회 API
     * [GET] /:familyId/users
     * @return BaseResponse<FamilyDto>
     */
    @NoAuthCheck
    @GetMapping("/{familyId}/users")
    @Operation(summary = "가족원 전체 조회", description = "현재 활동 중인 전체 가족 구성원을 조회합니다.")
    public BaseResponse<List<GetFamilyAllRes>> getFamilyAll(
            @PathVariable Long familyId) {
        try {
            List<GetFamilyAllRes> getFamilyAllRes = familyService.getFamilyAll(familyId);
            return new BaseResponse<>(getFamilyAllRes);
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 초대코드로 가족 정보 조회 API
     * [GET] /{inviteCode}/inviteCode
     * @return BaseResponse<FamilyDto>
     */
    @PostMapping("/inviteCode")
    public BaseResponse<FamilyIdDto> getFamilyByInviteCode(@RequestBody String inviteCode,
                                                         @AuthenticationPrincipal User user,
                                                         @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){
        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            FamilyIdDto familyIdDto = familyService.getFamilyByInviteCode(inviteCode);
            return new BaseResponse<>(familyIdDto);
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(FIND_FAIL_FAMILY);
        }
    }

    /**
     * 초대 API
     * [GET] /familyId
     * @return BaseResponse<String>
     */
    @PostMapping("/{familyId}")
    public BaseResponse<String> inviteUser(@PathVariable Long familyId,
                                           @RequestParam List<String> userIds,
                                           @AuthenticationPrincipal User user,
                                           @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){
        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        for(String ids : userIds) {
            System.out.println("\n + \n + \n + \n + \n + \n + \n + \n ");
            System.out.println(ids);
            System.out.println("\n + \n + \n + \n + \n + \n + \n + \n ");
        }

        try {
            familyService.inviteUser(user, userIds, familyId);
            return new BaseResponse<>("초대 요청이 완료되었습니다.");
        } catch (IllegalAccessException e) {
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.CONFLICT.value());
        } catch (NoSuchElementException e){
            e.printStackTrace();
            return new BaseResponse<>(FIND_FAIL_USERNAME);
        }
    }

    /**
     * 초대 승락 API
     * [GET] /{familyId}/invite-accept
     * @return BaseResponse<String>
     */
    @PatchMapping("/{familyId}/invite-accept")
    public BaseResponse<String> acceptFamily(@PathVariable Long familyId,
                                             @AuthenticationPrincipal User user,
                                             @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){

        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            familyService.acceptFamily(user, familyId);
            return new BaseResponse<>("초대가 수락되었습니다.");
        }catch (NoSuchElementException e){
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.NOT_FOUND.value());
        }
    }

    /**
     * 업로드 주기 수정 API
     * [PATCH] /:familyId?uploadCycle={업로드주기}
     * @return BaseResponse<String>
     */
    @NoAuthCheck
    @PatchMapping("/{familyId}")
    @Operation(summary = "업로드 주기 수정", description = "가족 업로드 알림 주기를 수정합니다.")
    public BaseResponse<String> updateUploadCycle(
            @AuthenticationPrincipal User user,
            @PathVariable Long familyId,
            @RequestParam("uploadCycle") int uploadCycle) {
        try {
            familyService.updateUploadCycle(user, familyId, uploadCycle);
            return new BaseResponse<>("업로드 주기가 수정되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /**
     * 가족 삭제 API
     * [DELETE] /:familyId
     * @return BaseResponse<String>
     */
    @NoAuthCheck
    @DeleteMapping("/{familyId}")
    @Operation(summary = "가족 삭제", description = "가족을 삭제합니다. 댓글, 게시글, 가족이 일괄 삭제됩니다.")
    public BaseResponse<String> deleteFamily(
            @AuthenticationPrincipal User user,
            @PathVariable Long familyId) {
        try {
            familyService.deleteFamily(user, familyId);
            return new BaseResponse<>("가족이 삭제되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /** 가족 정보수정 API
     * [GET] /families/{familyId}
     * @return BaseResponse<FamilyDto>
     */
    @PatchMapping("/{familyId}/update")
    public BaseResponse<FamilyDto> updateFamily(@PathVariable Long familyId,
                                                @AuthenticationPrincipal User user,
                                                @RequestBody FamilyUpdateDto familyUpdateDto,
                                                @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){

        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            FamilyDto resFamilyDto = familyService.updateFamily(user, familyId, familyUpdateDto);
            return new BaseResponse<>(resFamilyDto);
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(FIND_FAIL_USERNAME);
        }catch (IllegalAccessException e){
            return new BaseResponse<>(FAILED_USERSS_UNATHORIZED);
        }
    }

    /** 가족 탈퇴 API
     * [DELETE] /families/{familyId}/withdraw
     * @return BaseResponse<String>
     */
    @DeleteMapping("/{familyId}/withdraw")
    public BaseResponse<String> withdrawFamily(@PathVariable Long familyId,
                                               @AuthenticationPrincipal User user,
                                               @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){
        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            familyService.withdrawFamily(user, familyId);
            return new BaseResponse<>("가족에서 탈퇴되었습니다.");
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.NOT_FOUND.value());
        }catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /** 가족 강제 탈퇴 API
     * [DELETE] /families/{familyId}/emission
     * @return BaseResponse<String>
     */
    @DeleteMapping("/{familyId}/emission")
    public BaseResponse<String> emissionFamily(@PathVariable Long familyId,
                                               @AuthenticationPrincipal User user,
                                               @RequestParam List<String> userIds,
                                               @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){
        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            familyService.emissionFamily(user, familyId, userIds);
            return new BaseResponse<>("가족에서 탈퇴되었습니다.");
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.NOT_FOUND.value());
        } catch (BaseException e) {
            return new BaseResponse<>((e.getStatus()));
        }
    }

    /** 가족 권한 수정 API
     * [DELETE] /faimlies/{familyId}/authority
     */
    @PatchMapping("/{familyId}/authority")
    public BaseResponse<String> changeFamilyAuthority(@PathVariable Long familyId,
                                               @AuthenticationPrincipal User user,
                                               @RequestBody Map<String, String> map,
                                               @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){

        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            // 가족 권한 수정
            familyService.changeFamilyAuthority(user, familyId, map.get("userId"));
            return new BaseResponse<>("가족 대표가 변경되었습니다..");
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.NOT_FOUND.value());
        } catch (IllegalAccessException e) {
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.FORBIDDEN.value());
        }
    }

    @PostMapping("/{familyId}/insertMember")
    BaseResponse<String> insertMember(@PathVariable Long familyId,
                                               @AuthenticationPrincipal User user,
                                               @RequestHeader("X-AUTH-TOKEN") String requestAccessToken){

        if (authService.validate(requestAccessToken)) { //유효한 사용자라 true가 반환됩니다 !!
            return new BaseResponse<>(INVALID_JWT); //401 error : 유효한 사용자이지만, 토큰의 유효 기간이 만료됨.
        }
        if(user == null) {
            return new BaseResponse<>(INVALID_USER_JWT); //403 error : 유효한 사용자가 아님.
        }

        try {
            familyService.insertMember(user, familyId);
            return new BaseResponse<>("가족에 가입되었습니다");
        } catch (NoSuchElementException e) {
            return new BaseResponse<>(false, e.getMessage(), HttpStatus.NOT_FOUND.value());
        }
    }
}