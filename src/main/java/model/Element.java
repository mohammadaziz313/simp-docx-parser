package model;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "elements")
public class Element {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Getter
	private long id;
	@Getter
	@Setter
	@Column(name = "title")
	private String title;
	@Getter
	@Setter
	@Column(name = "data")
	private String data;
	// Add other fields below
}
