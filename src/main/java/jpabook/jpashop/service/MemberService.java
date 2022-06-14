package jpabook.jpashop.service;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;

    //가입
    @Transactional
    public Long join(Member member){
        //검증 필용 
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }
    
    //중복 회원 검증 로직 
    public void validateDuplicateMember(Member member) {
        List<Member> byName = memberRepository.findByName(member.getName());
        if(!byName.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원");
        }
    }

    //전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    //아이디 조회회
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }
    
    
    //수정
    //merge x
    //변경감지 O 
    @Transactional
    public void update(Long id, String name) {
        Member mebmer = memberRepository.findOne(id); //영속성 컨택스트에 올림 
        mebmer.setName(name); //바꾸고 -> 트잭 종료~ 반영시킴
    }
}
