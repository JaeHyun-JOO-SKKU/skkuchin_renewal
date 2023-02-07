package skkuchin.service.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import skkuchin.service.api.dto.CMRespDto;
import skkuchin.service.api.dto.FavoriteDto;
import skkuchin.service.domain.User.AppUser;
import skkuchin.service.repo.FavoriteRepo;
import skkuchin.service.repo.UserRepo;
import skkuchin.service.config.auth.PrincipalDetails;
import skkuchin.service.service.FavoriteService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/favorite")
@Slf4j
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserRepo userRepo;
    private final FavoriteRepo favoriteRepo;
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<?> write(@Valid @RequestBody FavoriteDto.PostRequest dto, @AuthenticationPrincipal PrincipalDetails principalDetails) {

        AppUser user = principalDetails.getUser();
        favoriteService.write(user, dto);
        return new ResponseEntity<>(new CMRespDto<>(1, "즐겨찾기 저장 완료", null), HttpStatus.CREATED);
    }



    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<?> getPlaceReview(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        AppUser user = principalDetails.getUser();
        List<FavoriteDto.Response> favoriteMenus = favoriteService.getMyFavorite(user);
        return new ResponseEntity<>(new CMRespDto<>(1, "즐겨찾기 조회 완료", favoriteMenus), HttpStatus.OK);
    }

    @DeleteMapping("/{favoriteId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long favoriteId, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        AppUser user = principalDetails.getUser();
        Long userId = user.getId();
        favoriteService.delete(favoriteId, userId);
        return new ResponseEntity<>(new CMRespDto<>(1, "즐겨찾기 삭제 완료", null), HttpStatus.OK);
    }
    //관리자 즐겨찾기 기능 추가
    //남의 즐겨찾기 강제 삭제 가능
    @DeleteMapping("/{userId}/{favoriteId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> adminDelete(@PathVariable Long favoriteId,@PathVariable Long userId) {
        favoriteService.delete(favoriteId, userId);
        return new ResponseEntity<>(new CMRespDto<>(1, "관리자 즐겨찾기 삭제 완료", null), HttpStatus.OK);
    }

}