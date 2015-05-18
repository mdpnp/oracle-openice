package org.mdpnp.hiberdds.mappings;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 	
 * @author diego@mdpnp.org
 *
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "NUMERIC_LIFECYCLE", indexes = {@Index(name="fk_lifecycle_numeric", columnList="id_numeric", unique=false)})
public class NumericLifeCycle implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "numer_lc_seq")
	@SequenceGenerator(name = "numer_lc_seq",  sequenceName = "numeric_lifecycle_seq", allocationSize = 1, initialValue = 1)
	private int id_numeric_lifecycle;
	
//	@JoinColumn(name="id_numeric")
//	private int id_numeric;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_numeric", nullable = false)
	private Numeric numeric;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIME")
	private Date time;
	
	@Column(name = "ALIVE")
	private int alive;
	
	
	public NumericLifeCycle(){}
	
	//TODO Add a parametriced constructor
	public NumericLifeCycle(boolean alive){
		this.alive = alive ? 1 : 0;
		
	}


	public int getId_numeric_lifecycle() {
		return id_numeric_lifecycle;
	}


	public void setId_numeric_lifecycle(int id_numeric_lifecycle) {
		this.id_numeric_lifecycle = id_numeric_lifecycle;
	}


//	public int getId_numeric() {
//		return id_numeric;
//	}
//
//
//	public void setId_numeric(int id_numeric) {
//		this.id_numeric = id_numeric;
//	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public int getAlive() {
		return alive;
	}


	public void setAlive(int alive) {
		this.alive = alive;
	}

	public Numeric getNumeric() {
		return numeric;
	}

	public void setNumeric(Numeric numeric) {
		this.numeric = numeric;
	}
	
	
	
	

}
