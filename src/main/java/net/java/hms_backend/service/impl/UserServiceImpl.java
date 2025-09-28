package net.java.hms_backend.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.UserDto;
import net.java.hms_backend.dto.UserFilterRequest;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.exception.UserException;
import net.java.hms_backend.mapper.UserMapper;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.UserRepository;
import net.java.hms_backend.service.AuditLogService;
import net.java.hms_backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserException.DuplicateEmailException("Email already exists: " + dto.getEmail());
        }

        List<Role> roles = roleRepository.findByNameIn(dto.getRoles());

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new UserException.MissingPasswordException("Password is required when creating user");
        }

        User user = UserMapper.toEntity(dto, roles);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(user);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Created user: [ID=" + savedUser.getId() +
                ", FullName=" + savedUser.getFullName() +
                ", Email=" + savedUser.getEmail() +
                ", Phone=" + savedUser.getPhoneNumber() +
                ", Roles=" + savedUser.getRoles().stream()
                .map(Role::getName)
                .toList() + "]";

        auditLogService.log(
                username,
                "CREATE",
                "User",
                savedUser.getId(),
                details
        );

        return UserMapper.toDto(savedUser);
    }


    @Override
    public Page<UserDto> getAllUsers(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Retrieved user list - Page: " + page +
                ", Size: " + size +
                ", Total: " + users.getTotalElements();

        auditLogService.log(
                username,
                "QUERY",
                "User",
                null,
                details
        );
        return users.map(UserMapper::toDto);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Retrieved user by ID: " + id +
                ", FullName: " + user.getFullName() +
                ", Email: " + user.getEmail() +
                ", Phone: " + user.getPhoneNumber();

        auditLogService.log(
                username,
                "QUERY",
                "User",
                user.getId(),
                details
        );
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", auth.getName()));

        boolean isAdminOrManager = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_ADMIN") ||
                                grantedAuthority.getAuthority().equals("ROLE_MANAGER"));

        if (!isAdminOrManager && !currentUser.getId().equals(id)) {
            throw new UserException.AccessDeniedException("You are not allowed to update this user");
        }

        if (!isAdminOrManager && dto.getRoles() != null) {
            throw new UserException.AccessDeniedException("You are not allowed to update roles");
        }

        StringBuilder changes = new StringBuilder("Updated user ID: ").append(id).append(". Changes: ");

        if (dto.getPassword() != null) {
            if (dto.getPassword().isBlank()) {
                throw new UserException.MissingPasswordException("Password must not be blank");
            }
            changes.append("password updated; ");
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (isAdminOrManager && dto.getRoles() != null) {
            List<Role> roles = roleRepository.findByNameIn(dto.getRoles());
            changes.append("roles updated to: ").append(dto.getRoles()).append("; ");
            existing.setRoles(roles);
        }

        if (dto.getFullName() != null && !dto.getFullName().equals(existing.getFullName())) {
            changes.append("fullName: ").append(existing.getFullName())
                    .append(" → ").append(dto.getFullName()).append("; ");
            existing.setFullName(dto.getFullName());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(existing.getPhoneNumber())) {
            changes.append("phoneNumber: ").append(existing.getPhoneNumber())
                    .append(" → ").append(dto.getPhoneNumber()).append("; ");
            existing.setPhoneNumber(dto.getPhoneNumber());
        }

        User updatedUser = userRepository.save(existing);

        String username = auth.getName();

        auditLogService.log(
                username,
                "UPDATE",
                "User",
                updatedUser.getId(),
                changes.toString()
        );

        return UserMapper.toDto(updatedUser);
    }



    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String details = "Deleted user: [ID=" + user.getId() +
                ", FullName=" + user.getFullName() +
                ", Email=" + user.getEmail() +
                ", Phone=" + user.getPhoneNumber() +
                ", Roles=" + user.getRoles().stream()
                .map(Role::getName)
                .toList() + "]";

        auditLogService.log(
                username,
                "DELETE",
                "User",
                user.getId(),
                details
        );

        userRepository.deleteById(id);
    }


    private List<Predicate> buildUserPredicates(UserFilterRequest filter, Root<User> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getFullName() != null && !filter.getFullName().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("fullName")),
                    "%" + filter.getFullName().toLowerCase() + "%"
            ));
        }

        if (filter.getPhoneNumber() != null && !filter.getPhoneNumber().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("phoneNumber")),
                    "%" + filter.getPhoneNumber().toLowerCase() + "%"
            ));
        }

        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            predicates.add(cb.like(
                    cb.lower(root.get("email")),
                    "%" + filter.getEmail().toLowerCase() + "%"
            ));
        }

        if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            predicates.add(roleJoin.get("name").in(filter.getRoles()));
        }

        return predicates;
    }


    @Override
    public Page<UserDto> searchUsers(UserFilterRequest filter, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<Predicate> predicates = buildUserPredicates(filter, user, cb);
        query.select(user).where(cb.and(predicates.toArray(new Predicate[0]))).distinct(true);

        List<User> resultList = entityManager.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);

        List<Predicate> countPredicates = buildUserPredicates(filter, countRoot, cb);
        countQuery.select(cb.countDistinct(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        Long total = entityManager.createQuery(countQuery).getSingleResult();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        StringBuilder details = new StringBuilder("Filtered users with criteria: ");

        if (filter.getFullName() != null) {
            details.append("fullName=").append(filter.getFullName()).append("; ");
        }
        if (filter.getPhoneNumber() != null) {
            details.append("phoneNumber=").append(filter.getPhoneNumber()).append("; ");
        }
        if (filter.getEmail() != null) {
            details.append("email=").append(filter.getEmail()).append("; ");
        }
        if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
            details.append("roles=").append(filter.getRoles()).append("; ");
        }

        details.append("Page=").append(page)
                .append(", Size=").append(size)
                .append(", TotalResults=").append(total);

        auditLogService.log(
                username,
                "FILTER",
                "User",
                null,
                details.toString()
        );
        List<UserDto> dtos = resultList.stream().map(UserMapper::toDto).toList();
        return new PageImpl<>(dtos, PageRequest.of(page, size), total);
    }


    @Override
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        String details = "Retrieved current user profile: [ID=" + user.getId() +
                ", FullName=" + user.getFullName() +
                ", Email=" + user.getEmail() +
                ", Phone=" + user.getPhoneNumber() + "]";

        auditLogService.log(
                email,
                "QUERY",
                "User",
                user.getId(),
                details
        );
        return UserMapper.toDto(user);
    }
}
