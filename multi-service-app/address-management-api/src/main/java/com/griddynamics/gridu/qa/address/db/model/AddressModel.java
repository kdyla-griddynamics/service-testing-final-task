package com.griddynamics.gridu.qa.address.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Address")
@Table(name = "address")
public class AddressModel {

  @Id
  private Long id;
  @Column(name = "user_id")
  private Long userId;
  @Column
  private String zip;
  @Column
  private String state;
  @Column
  private String city;
  @Column
  private String line_one;
  @Column
  private String line_two;

}
