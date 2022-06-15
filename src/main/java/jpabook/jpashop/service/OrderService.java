package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /** 주문 생성 */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        //엔티티 조회
        //멤버 갖고옴
        Member member = memberRepository.findOne(memberId); //영컨 에 슉
        //아이템 갖고옴
        Item item = itemRepository.findOne(itemId); //영컨에 슉

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);
        //배송셋팅 완료

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);


        //주문 생성
        //비영속상태
        //주문 객체 정보가 생김.
        Order order = Order.createOrder(member, delivery, orderItem); //비영속 상태



        //주문 저장
        //--> 들어가면 em.persist(order)
        // 비영속 --> 영속으로 바뀜  --> 해당 transaction 끝나면서 반영
        //주문 (delivery,ordetItem) 다 전파 저장
        orderRepository.save(order);

        return order.getId();
    }
    /** 주문 취소 */
    @Transactional
    public void cancelOrder(Long orderId) {

        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId); //영컨에 슉

        //주문 취소
        order.cancel();


    }
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}