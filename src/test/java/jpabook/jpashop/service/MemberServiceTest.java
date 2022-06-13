package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
//    @Autowired EntityManager em;
    @Test
    public void 회원가입() throws Exception{
        //g
        Member member = new Member();
        member.setName("eom1");

        //w
        Long join = memberService.join(member);
//        em.flush();  쿼리확인

        //t
        assertEquals(member,memberRepository.findOne(join));
    }

    @Test(expected = IllegalStateException.class) //try -catch 대신
    public void 중복회원예외() throws Exception{
        //g
        Member member1 = new Member();
        member1.setName("kim");
        Member member2 = new Member();
        member2.setName("kim");

        //w
        memberService.join(member1);
        //여기 try catch 들어가야 하는데, expected로 대체
        memberService.join(member2);//예외가 발생해야함

        //t
        //에가 실행되면 실패 Assertions.
        fail("예외가 발생해야함");
    }

}