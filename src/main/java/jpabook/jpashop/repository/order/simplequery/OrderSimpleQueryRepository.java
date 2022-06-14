package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    //-->이렇게 한 이유 : Repository는 순수 Entity만을 위해


    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery("select " +
                        "new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto" +
                        "(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d ", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
