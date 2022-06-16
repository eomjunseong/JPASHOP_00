package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    //OrderSimpleController 에서 지원하지 않던, 주문상품의 상세내역까지

    private final OrderRepository orderRepository;


    //V4용
    private final OrderQueryRepository orderQueryRepository;

    //V1- entity
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            order.getMember().getName(); //주문에서 --> 주문자 이름(LAZY여서 프록시 객체 갖고 있음 ->직접 초기화)
            order.getDelivery().getAddress(); //주문에서 -->배송정보(LAZY여서 프록시 객체 갖고 있음 ->직접 초기화)
            List<OrderItem> orderItems = order.getOrderItems();//주문에서 --> orderItem(디폴트 LAZY여서 직접 초기화 필요)

            //OrdetItem에 Item이 LAZY임 --> 직접 초기화 필요
            orderItems.stream().forEach(a -> a.getItem().getName()); //getItem()이 아니라, getName()에서 초기화됨 ㅎㅎ
        }
        return orders;
    }

    //V2 DTO
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(OrderDto::new).collect(toList());
    }


    //V3 JOIN FETCH
    //단점 : 1대다 페이징 볼가능
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    //V3.1 페이징 가능하게/
    //application.yml : default_batch_fetch_size: 100
    //in 쿼리 날라감
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    //V4
    //컬렉션 조회 DTO n+n? n+1문제
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    //V5
    //컬렉션 조회 DTO 최적화
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    //V6
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream().collect(groupingBy(o ->
                new OrderQueryDto(o.getOrderId(),o.getName(),
                                        o.getOrderDate(),
                                        o.getOrderStatus(),
                                        o.getAddress()),
                                        mapping(o ->
                                                new OrderItemQueryDto(
                                                                        o.getOrderId(),
                                                                        o.getItemName(),
                                                                        o.getOrderPrice(),
                                                                        o.getCount()),toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(),
                        e.getKey().getOrderDate(),
                        e.getKey().getOrderStatus(),
                        e.getKey().getAddress(),
                        e.getValue())).collect(toList());
    }







    //DTO
    //OrderItemDto
    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; //List<OrdetItem> 이라고 하면 안됨. -> 이거 entity 노출임 !!!!

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count; //주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
