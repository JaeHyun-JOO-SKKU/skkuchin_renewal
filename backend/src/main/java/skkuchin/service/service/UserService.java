package skkuchin.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import skkuchin.service.api.dto.EmailAuthRequestDto;
import skkuchin.service.api.dto.UserDto;
import skkuchin.service.domain.User.*;
import skkuchin.service.exception.DiscordException;
import skkuchin.service.exception.DuplicateException;
import skkuchin.service.exception.EmailAuthNumNotFoundException;
import skkuchin.service.repo.*;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final EmailAuthRepo emailAuthRepo;
    private final KeywordRepo keywordRepo;
    private final UserKeywordRepo userKeywordRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public boolean checkUsername(String username) {
        AppUser existingUser = userRepo.findByUsername(username);
        if (existingUser == null) {
            return false;
        } else return true;
    }

    public AppUser saveUser(UserDto.SignUpForm signUpForm) throws MessagingException, UnsupportedEncodingException {
        log.info("Saving new user {} to the database", signUpForm.getNickname());
        if (!signUpForm.getPassword().equals(signUpForm.getRePassword())) {
            throw new DiscordException("re_password_error");
        }
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        AppUser appUser = signUpForm.toEntity();
        appUser.getRoles().add(roleRepo.findByName("ROLE_USER"));
        AppUser newUser = userRepo.save(appUser);
        emailService.sendEmail(newUser.getEmail());
        return newUser;
    }

    public void saveAdmin(UserDto.SignUpForm signUpForm) {
        if (userRepo.findByUsername("admin") == null) {
            signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
            AppUser appUser = signUpForm.toEntity();
            appUser.getRoles().add(roleRepo.findByName("ROLE_ADMIN"));
            appUser.emailVerifiedSuccess();
            userRepo.save(appUser);
        }
    }

    public void saveTestUser(UserDto.SignUpForm signUpForm) {
        if (userRepo.findByUsername("test") == null) {
            signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
            AppUser appUser = signUpForm.toEntity();
            appUser.getRoles().add(roleRepo.findByName("ROLE_USER"));
            appUser.emailVerifiedSuccess();
            userRepo.save(appUser);
        }
    }

    public Boolean confirmEmail(EmailAuthRequestDto requestDto) {
        EmailAuth emailAuth = emailAuthRepo.findByEmailAndAuthNumAndExpireDateAfter(
                requestDto.getEmail(), requestDto.getAuthNum(), LocalDateTime.now())
                .orElseThrow(() -> new EmailAuthNumNotFoundException());
        AppUser user = userRepo.findByEmail(requestDto.getEmail());
        emailAuth.useToken();
        user.emailVerifiedSuccess();
        return true;
    }

    public void saveRole(Role role) {
        String roleName = role.getName();
        if (roleRepo.findByName(roleName) == null) {
            log.info("Saving new role {} to the database", role.getName());
            roleRepo.save(role);
        }
    }

    public Role getRole(String roleName) {
        log.info("Fetching role {}", roleName);
        return roleRepo.findByName(roleName);
    }

    public void addRoleToUser(String username, String roleName) {
        log.info("Adding role {} to user {}", roleName, username);
        AppUser user = userRepo.findByUsername(username);
        Role role = roleRepo.findByName(roleName);
        //user.getRoles().add(role);
        if (user.getRoles().contains(role)) {
            throw new DuplicateException("role_duplicate_error");
        } else {
            user.getRoles().add(role);
        }
    }

    public UserDto.Response getUser(String username) {
        log.info("Fetching user {}", username);
        return new UserDto.Response(userRepo.findByUsername(username));
    }

    public AppUser getUserForRefresh(String username) {
        return userRepo.findByUsername(username);
    }

    public List<UserDto.Response> getUsers() {
        log.info("Fetching all users");
        return userRepo.findAll()
                .stream()
                .map(user -> new UserDto.Response(user))
                .collect(Collectors.toList());
    }

    public void addInfo(AppUser user, UserDto.AdditionalRequest dto) {
        AppUser existingUser = userRepo.findById(user.getId()).orElseThrow();
        existingUser.setGender(dto.getGender());
        existingUser.setMbti(dto.getMbti());
        existingUser.setImage(dto.getImage());
        userRepo.save(existingUser);

        List<UserKeyword> userKeywords = dto.getKeywords()
                .stream()
                .map(k -> {
                    Keyword keyword = keywordRepo.findByName(k);
                    return dto.toUserKeywordEntity(user, keyword);
                })
                .collect(Collectors.toList());
        userKeywordRepo.saveAll(userKeywords);
    }
}
