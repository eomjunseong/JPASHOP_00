package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 *
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 *
 *
 * 즉 컬렉션 뺴고임~
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    
    //V4용
    //-->이렇게 한 이유 : Repository는 순수 Entity만을 위해
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    //V1 주문 조회
    //hibernate5 or properties에 spring.jackson.serialization.fail-on-empty-beans= false
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화 --> .getMember() 까지 프록시, getName();에서 쿼리
            order.getDelivery().getAddress(); //Lazy 강제 초기화 --> 이하 동일 ( 이런게 강제 초기화~)
        }
        return all;
    }

    //V2 DTO
    //Result로 감싸야함
    //쿼리가 여러번 나가욤~
    @GetMapping("/api/v2/simple-orders")
    public Result<List<SimpleOrderDto>> ordersV2(){

        //전체 조회
        // 두개 조회됨
        List<Order> orders = orderRepository.findAll();

        //두번 실행됨
        //그안에 멤버 두번
        //동시에 딜리버리 두번
        //토탈 쿼리 5번
        List<SimpleOrderDto> collect = orders.stream().map(SimpleOrderDto::new).collect(toList());
        return new Result<>(collect.size(), collect);
    }


    //V3
    //JOIN FETCH : qeury 양을 줄임 : 5개 쿼리 1개 쿼리로 끝....
    //특징 select * from order....에서
    //     member + delivery 정보다 모든게 실려있음
    //v4? : 필요한거만 실어옴 (join fetch 사용 X)
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        return result;
    }
    
    
/*
--> 해당기능은 
-->  orderRepository.findOrderDtos(); 에서 
-->  orderSimpleQueryRepository.findOrderDtos(); 로 이동 



    //V4 - 조회
    //필요한것만 실어옴
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }


-->이렇게 한 이유 : Repository는 순수 Entity만을 위해
*/

    //V4 - 조회
    //필요한것만 실어옴
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }


    /*
*  DTO ~ RESULT
*
* */
    //V2 DTO
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화(V2) // V3경우 이미 같이 불러옴
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }
    //RESULT
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count ;  //{}객체로 싸서 반환 하는 장점임
        private T data;
    }
}
