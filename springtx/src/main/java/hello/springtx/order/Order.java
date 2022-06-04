package hello.springtx.order;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "orders") // order by 로 인해 order 가 예약어라서 order table 이 생성 불가능한 경우가 많기 때문이다.
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String username;  // 정상, 에외, 잔고부족
    private String payStatus; // 대기, 완료
}
