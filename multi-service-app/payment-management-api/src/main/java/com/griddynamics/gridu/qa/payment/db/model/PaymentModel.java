package com.griddynamics.gridu.qa.payment.db.model;

import static org.springframework.util.StringUtils.isEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Payment")
@Table(name = "payment")
public class PaymentModel {

  public static final String FAILED_TOKEN = "FAILED";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "user_id")
  private Long userId;
  @Column(name = "card_number")
  private String cardNumber;
  @Column
  private String cardholder;
  @Column(name = "expiry_year")
  private Integer expiryYear;
  @Column(name = "expiry_month")
  private Integer expiryMonth;
  @Column
  private String cvv;
  @Column
  @EqualsAndHashCode.Exclude
  private String token;

  public Boolean isTokenFilled() {
    return !isEmpty(token) && !FAILED_TOKEN.equals(token);
  }
}
