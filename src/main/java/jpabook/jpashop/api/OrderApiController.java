package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    //OrderSimpleController 에서 지원하지 않던, 주문상품의 상세내역까지

    private final OrderRepository orderRepository;

    //V1- entity
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            order.getMember().getName(); //주문에서 --> 주문자 이름(LAZY여서 프록시 객체 갖고 있음 ->직접 초기화)
            order.getDelivery().getAddress(); //주문에서 -->배송정보(LAZY여서 프록시 객체 갖고 있음 ->직접 초기화)
            List<OrderItem> orderItems = order.getOrderItems();//주문에서 --> orderItem(디폴트 LAZY여서 직접 초기화 필요)

            //OrdetItem에 Item이 LAZY임 --> 직접 초기화 필요
            orderItems.stream().forEach(a->a.getItem().getName()); //getItem()이 아니라, getName()에서 초기화됨 ㅎㅎ
        }
        return orders;
    }

    //V2 DTO
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(OrderDto::new).collect(toList());
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
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
