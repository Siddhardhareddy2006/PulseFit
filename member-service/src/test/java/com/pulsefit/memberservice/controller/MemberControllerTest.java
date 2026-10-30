package com.pulsefit.memberservice.controller;

import com.pulsefit.memberservice.entity.Member;
import com.pulsefit.memberservice.repo.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    MemberRepository repo;

    @InjectMocks
    MemberController controller;

    @Test
    void allReturnsMembers() {

        Member member = new Member();
        member.id = 1L;

        when(repo.findAll()).thenReturn(java.util.Collections.singletonList(member));

        List<Member> result = controller.all();

        assertEquals(1, result.size());
        assertSame(member, result.get(0));
    }

    @Test
    void oneReturnsMember() {

        Member member = new Member();
        member.id = 1L;

        when(repo.findById(1L))
                .thenReturn(Optional.of(member));

        Member result = controller.one(1L);

        assertSame(member, result);
    }

    @Test
    void oneMissingThrows404() {

        when(repo.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.one(99L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void addCreatesMember() {

        Member member = new Member();

        member.firstName = "Test";
        member.lastName = "User";
        member.email = "test@example.com";

        when(repo.save(member))
                .thenReturn(member);

        org.springframework.http.ResponseEntity<Member> response = controller.add(member);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertSame(
                member,
                response.getBody()
        );

        verify(repo).save(member);
    }

    @Test
    void updateCopiesFieldsAndSaves() {

        Member existing = new Member();

        existing.id = 1L;
        existing.firstName = "Old";

        Member update = new Member();

        update.firstName = "New";
        update.lastName = "User";
        update.email = "new@example.com";
        update.phone = "123";
        update.gender = "MALE";
        update.dateOfBirth = "2000-01-01";
        update.facility = "Central";
        update.membershipStatus = "ACTIVE";

        when(repo.findById(1L))
                .thenReturn(Optional.of(existing));

        when(repo.save(existing))
                .thenReturn(existing);

        Member result = controller.update(1L, update);

        assertEquals("New", result.firstName);
        assertEquals("User", result.lastName);
        assertEquals("new@example.com", result.email);
        assertEquals("123", result.phone);
        assertEquals("MALE", result.gender);
        assertEquals("2000-01-01", result.dateOfBirth);
        assertEquals("Central", result.facility);
        assertEquals("ACTIVE", result.membershipStatus);

        verify(repo).save(existing);
    }

    @Test
    void deleteExistingReturnsNoContent() {

        when(repo.existsById(1L))
                .thenReturn(true);

        org.springframework.http.ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(repo).deleteById(1L);
    }

    @Test
    void deleteMissingThrows404() {

        when(repo.existsById(1L))
                .thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> controller.delete(1L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(repo, never()).deleteById(1L);
    }

    @Test
    void searchUsesRepositoryQuery() {

        Member member = new Member();

        when(
                repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        "rahul",
                        "rahul"
                )
        ).thenReturn(java.util.Collections.singletonList(member));

        List<Member> result =
                controller.search("rahul");

        assertEquals(1, result.size());
        assertSame(member, result.get(0));
    }

    @Test
    void existsReturnsExpectedMap() {

        when(repo.existsById(1L))
                .thenReturn(true);

        Map<String, Object> result =
                controller.exists(1L);

        assertEquals(1L, result.get("memberId"));
        assertEquals(true, result.get("exists"));
    }
}