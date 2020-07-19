package com.upgrade.challenge.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DailyOccupation implements Serializable {

	private static final long serialVersionUID = 8719778416877659361L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(unique = true)
	private String date;
	
	@Column
	@JsonProperty("availability")
	private Integer guests;
	
	@Version
	private int version;

	public DailyOccupation() {}
	
	public DailyOccupation(String date, Integer guests) {
		this.date = date;
		this.guests = guests;
	}
	
}
