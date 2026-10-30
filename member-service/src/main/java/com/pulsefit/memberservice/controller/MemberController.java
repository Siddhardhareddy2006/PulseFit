package com.pulsefit.memberservice.controller;

import com.pulsefit.memberservice.entity.Member;
import com.pulsefit.memberservice.repo.MemberRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository repo;

    public MemberController(MemberRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Member> all() {
        return repo.findAll();
    }

    @GetMapping("/search")
    public List<Member> search(
            @RequestParam(defaultValue = "") String name) {

        return repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                name,
                name
        );
    }

    @GetMapping("/{id}")
    public Member one(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found"
                        )
                );
    }

    @PostMapping
    public ResponseEntity<Member> add(
            @Valid @RequestBody Member member) {

        Member saved = repo.save(member);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public Member update(
            @PathVariable Long id,
            @Valid @RequestBody Member member) {

        Member existing = one(id);

        existing.firstName = member.firstName;
        existing.lastName = member.lastName;
        existing.email = member.email;
        existing.phone = member.phone;
        existing.gender = member.gender;
        existing.dateOfBirth = member.dateOfBirth;
        existing.facility = member.facility;
        existing.membershipStatus = member.membershipStatus;

        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (!repo.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found"
            );
        }

        repo.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    public Map<String, Object> exists(@PathVariable Long id) {

        Map<String, Object> result = new HashMap<>();
        result.put("memberId", id);
        result.put("exists", repo.existsById(id));
        return result;
    }
}