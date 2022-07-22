package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //기본 생성자를 만듬 --> 생성을 막는다
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //ToMany  : LAZY default
    @OneToMany(fetch=LAZY,  mappedBy = "order", cascade = CascadeType.ALL) //Order가 저장되면 --> 얘도 같이 저장해라~
    private List<OrderItem> orderItems = new ArrayList<>(); //관례상 빈 ArrayList넣어줌

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    //==연관관계 메서드==//
    // 딱히 주인이라 얘가 해주는게 아니라 로직상
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    //Orderitem 저장
    // 주인이 아니지만 로직상 얘가 해줌
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    //배송 저장
    //딱히 주인이라 얘가 해주는게 아니라 로직상
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//ㅋ
    //주문 생성
    // delivery, orderItem 저장 되야함
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order(); //주문 만들고
        order.setMember(member); //멤버 셋팅 --cascade>? 따로 x 이미 저장된거라
        order.setDelivery(delivery); //딜리버리 -- CascadeType.ALL
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);//주문에 OrderItem 넣고 // OrderItem에도 같이 넣어주는 함수임
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order; //주문 객체를 생성해서 넘김 -->
    }

    //==비즈니스 로직==//
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            //RunTimeException
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}