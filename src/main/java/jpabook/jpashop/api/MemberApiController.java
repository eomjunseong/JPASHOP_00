package jpabook.jpashop.api;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController//ResponseBody + Controller
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

//가입
    //v1
    //entity 직접 노출 O
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }



    //v2
    //entity 직접 노출 X
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member); //저장 시키고
        return new CreateMemberResponse(id); //아이디반환
    }



//수정
    //v2 (v1없음)
    //DTO
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                                @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

//조회
    //v1 - entity
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    //v2 - DTO -  {} 껍대기 씌워서 반환                   {}:객체 []:배열
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        //엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect.size(),collect);
    }
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count ;  //{}객체로 싸서 반환 하는 장점임
        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


    @Data @AllArgsConstructor
    @NoArgsConstructor // 이거 필수네...
    static class CreateMemberRequest {
        private String name;
    }
    @Data @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }


    @Data @AllArgsConstructor
    @NoArgsConstructor
    static class UpdateMemberRequest{
        private String name;
    }
    @Data @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }
}