package com.parking.identity_service.service;

import com.parking.identity_service.dto.request.*;
import com.parking.identity_service.dto.response.*;
import com.parking.identity_service.entity.Role;
import com.parking.identity_service.entity.User;
import com.parking.identity_service.enums.EBlock;
import com.parking.identity_service.enums.ERole;
import com.parking.identity_service.exception.AppException;
import com.parking.identity_service.exception.ErrorCode;
import com.parking.identity_service.mapper.ProfileMapper;
import com.parking.identity_service.mapper.UserMapper;
import com.parking.identity_service.repository.RoleRepository;
import com.parking.identity_service.repository.UserRepository;
import com.parking.identity_service.repository.httpclient.ProfileClient;
import com.parking.identity_service.repository.httpclient.TicketClient;
import com.parking.identity_service.repository.httpclient.VaultClient;
import com.parking.identity_service.utils.PageUtils;
import com.parking.identity_service.utils.RandomUtils;
import com.parking.identity_service.validator.ValidEmail;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    ProfileMapper profileMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    VaultClient vaultClient;
    AuthenticationService authenticationService;
    KafkaTemplate<String, Object> kafkaTemplate;

    @PreAuthorize("hasAnyAuthority('EDIT_USER')")
    public List<UserResponse> blockAccountById(BlockUserRequest request) {

        List<User> users = userRepository.findAllById(request.getListUid());

        users.forEach(user ->
                user.setIsBlocked(EBlock.BLOCKED.getValue()));

        return userRepository.saveAll(users)
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public UserResponse createStaff(StaffCreationRequest request) {
        User user = userMapper.staffCreationRequestToUser(request);

        Set<String> defaultRoles = new HashSet<>();
        defaultRoles.add(ERole.STAFF.name());

        user = create(user, defaultRoles);

        return userMapper.toUserResponse(user);
    }

    public UserResponse createCustomerByGoogleAccount(GoogleAuthenticationRequest request) {
        GoogleUserProfileResponse googleUserProfileResponse =
                authenticationService.getInfoGoogleAccount(request.getAuthorizationCode(), request.getCodeVerifier());


        CustomerCreationRequest customerCreationRequest = CustomerCreationRequest.builder()
                .email(googleUserProfileResponse.getEmail())
                .password(RandomUtils.randomPassword())
                .build();

        return createCustomer(customerCreationRequest);
    }

    public UserResponse createCustomer(CustomerCreationRequest request) {

        if (!ValidEmail.isValidEmail(request.getEmail()))
            throw new AppException(ErrorCode.INVALID_EMAIL);

        User user = userMapper.customerCreationRequestToUser(request);

        Set<String> defaultRoles = new HashSet<>();
        defaultRoles.add(ERole.CUSTOMER.name());

        user = create(user, defaultRoles);

        CustomerProfileCreationRequest profileCreationRequest = CustomerProfileCreationRequest.builder()
                .name("New user")
                .uid(user.getUid())
                .build();

        profileClient.customerCreateProfile(profileCreationRequest);


        vaultClient.createOwner(OwnerCreationRequest.builder()
                .uid(user.getUid())
                .build());

//        kafkaTemplate.send("create-customer", EventCustomerCreate.builder()
//                .email(user.getEmail())
//                .build());

        return userMapper.toUserResponse(user);
    }

    User create(User user, Set<String> defaultRoles) {

        List<Role> roles = roleRepository.findAllById(defaultRoles);

        user.setIsBlocked(EBlock.NOT_BLOCK.getValue());
        user.setRoles(new HashSet<>(roles));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return user;
    }

    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }


    public List<User> getListUser(List<String> emails) {
        return userRepository.findAllByEmailIn(emails);
    }

    TicketClient ticketClient;

    public thong_tin_tai_khoan thong_tin_tai_khoan(String id) {

        thong_tin_tai_khoan thongTinTaiKhoan = new thong_tin_tai_khoan();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        thongTinTaiKhoan.setId(user.getUid());
        thongTinTaiKhoan.setEmail(user.getEmail());
        thongTinTaiKhoan.setStatus(user.getIsBlocked() == 1 ? "Đã khoá" : "Đang hoạt động");


        try {
            List<ProfileCustomer> profileCustomers = profileClient.getByListId(List.of(user.getUid())).getResult();
            if (profileCustomers.size() == 1) {

                thongTinTaiKhoan.setName(profileCustomers.getFirst().getName());
                thongTinTaiKhoan.setPhone(profileCustomers.getFirst().getPhone());

            }

            BalanceResponse balanceResponse = vaultClient.getBalance(user.getUid()).getResult();

            thongTinTaiKhoan.setSo_du(balanceResponse.getBalence());

            thongTinTaiKhoan.setTieu_dung_trong_thang(vaultClient.useinmonth(user.getUid()).getResult());
            thongTinTaiKhoan.setSo_ve_da_mua(vaultClient.useinmonth(user.getUid()).getResult());
            thongTinTaiKhoan.setSo_ve_da_mua(ticketClient.countTicketPurchased(user.getUid()).getResult());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return thongTinTaiKhoan;
    }

    public List<DanhSachTaiKhoanResponse> layDsTK(String name, String status, int page) {
        int limit = 30;

        List<ProfileCustomer> profiles = null;
        List<String> ids;
        List<User> users;
        if (!name.isEmpty()) {
            profiles = profileClient.getByName(name, page).getResult();
        }

        if (profiles != null) {
            ids = profiles.stream().map(ProfileCustomer::getUid).toList();
            users = userRepository.findAllByUidIn(ids);
        } else {
            if (status.equalsIgnoreCase("block")) {
                users = userRepository.findAllByIsBlocked(1, PageUtils.getPageable(page, limit, PageUtils.getSort("ASC", "uid")));
            } else if (status.equalsIgnoreCase("active")) {
                users = userRepository.findAllByIsBlocked(0, PageUtils.getPageable(page, limit, PageUtils.getSort("ASC", "uid")));
            } else {
                users = userRepository.findAll(PageUtils.getPageable(page, limit, PageUtils.getSort("ASC", "uid"))).getContent();
            }

            ids = users.stream().map(User::getUid).toList();

            profiles = profileClient.getByListId(ids).getResult();
        }


        users.sort(Comparator.comparing(User::getUid));
        profiles.sort(Comparator.comparing(ProfileCustomer::getUid));


        List<DanhSachTaiKhoanResponse> response = new ArrayList<>();
        int index_profile = 0;
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            ProfileCustomer p = profiles.get(index_profile);

            if (i > profiles.size()) {
                DanhSachTaiKhoanResponse item = DanhSachTaiKhoanResponse.builder()
                        .id(u.getUid())
                        .email(u.getEmail())
                        .status(u.getIsBlocked() == 1 ? "Đã khoá" : "Đang hoạt động")
                        .build();
                response.add(item);
            } else if (u.getUid().equalsIgnoreCase(p.getUid())) {
                DanhSachTaiKhoanResponse item = DanhSachTaiKhoanResponse.builder()
                        .id(u.getUid())
                        .sdt(p.getPhone())
                        .name(p.getName())
                        .email(u.getEmail())
                        .status(u.getIsBlocked() == 1 ? "Đã khoá" : "Đang hoạt động")
                        .build();
                index_profile++;
                response.add(item);
            } else {
                System.out.println("else___");
            }
        }

        return response;
    }
}
