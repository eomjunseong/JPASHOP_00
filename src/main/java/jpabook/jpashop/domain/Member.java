package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Data
public class Member {

    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;

    @NotEmpty //-->@valid로 체크
    private String name;

    @Embedded //@Embadable....
    private Address address;

    @JsonIgnore // 양방향 참조시에 한쪽에 해줘야함 그래서 연쇄 호출안함
    @OneToMany(mappedBy = "member") //읽기만 가능
    private List<Order> orders = new ArrayList<>();
}
