package com.ssafy.stellar.user.service;

import com.ssafy.stellar.fcm.entity.DeviceTokenEntity;
import com.ssafy.stellar.fcm.repository.DeviceTokenRepository;
import com.ssafy.stellar.user.dto.request.SignUpDto;
import com.ssafy.stellar.user.dto.response.UserDto;
import com.ssafy.stellar.user.entity.UserEntity;
import com.ssafy.stellar.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final DeviceTokenRepository deviceTokenRepository;

    private final PasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           DeviceTokenRepository deviceTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.bCryptPasswordEncoder = passwordEncoder;
    }

    @Override
    public void signUp(SignUpDto signUpDto) {
        String encodePassword = bCryptPasswordEncoder.encode(signUpDto.getPassword());
        String userId = signUpDto.getUserId();

        if (userRepository.findByUserId(userId) != null) {
            throw new IllegalArgumentException("User already exists with id: " + userId);
        }

        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setPassword(encodePassword);
        userRepository.save(user);
    }

    @Override
    public UserDto logIn(String userId, String password, String deviceToken) {
        UserEntity user = validateUser(userId);

        if (checkPassword(userId, password)) {
            UserDto userDto = new UserDto();
            userDto.setUserId(user.getUserId());
            userDto.setName(user.getName());
            userDto.setGender(user.getGender());
            saveDeviceToken(deviceToken, user);

            return userDto;
        } else {
            throw new IllegalArgumentException("Invalid password");
        }
    }

    @Override
    public void deleteUser(String userId, String password) {
        validateUser(userId);
        if (!checkPassword(userId, password)) {
            throw new IllegalArgumentException("아이디 비밀번호를 잘못 입력하였습니다.");
        }
        userRepository.deleteById(userId);
    }

    public void saveDeviceToken(String deviceToken, UserEntity user) {
        if (deviceToken == null) {
            return;
        }
        DeviceTokenEntity deviceTokenEntity = deviceTokenRepository.findByDeviceTokenAndUser(deviceToken, user);
        if (deviceTokenEntity == null) {
            deviceTokenEntity = new DeviceTokenEntity();
            deviceTokenEntity.setUser(user);
            deviceTokenEntity.setDeviceToken(deviceToken);
        }
        deviceTokenEntity.setLastLogin(LocalDateTime.now());  // 마지막 로그인 시간 설정
        deviceTokenRepository.save(deviceTokenEntity);
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        UserEntity user = validateUser(userId);
        return bCryptPasswordEncoder.matches(password, user.getPassword());
    }

    private UserEntity validateUser(String userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        return user;
    }
}
