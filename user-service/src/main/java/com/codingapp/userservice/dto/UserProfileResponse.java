package com.codingapp.userservice.dto;

import com.codingapp.userservice.model.UserTier;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse implements Serializable {

    private String username;
    private String email;
    private UserTier userTier;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private int totalSolved;

}
