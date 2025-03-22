package site.easy.to.build.crm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.service.user.UserProfileService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

@RestController
@RequestMapping("/api/profile")
public class UserProfileRestController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final AuthenticationUtils authenticationUtils;

    public UserProfileRestController(UserService userService, UserProfileService userProfileService, AuthenticationUtils authenticationUtils) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.authenticationUtils = authenticationUtils;
    }

    // Endpoint pour récupérer le profil utilisateur
    @GetMapping
    public UserProfile getUserProfile(Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        return userProfileService.findByUserId(userId);
    }

    // Endpoint pour mettre à jour le profil utilisateur
    @PutMapping("/update")
    public UserProfile updateUserProfile(@RequestBody UserProfile profile, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        profile.setUser(user);
        return userProfileService.save(profile);
    }
}
