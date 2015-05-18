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
 * @author diego@mdpnp.org
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "NUMERIC_SAMPLE", indexes = {@Index(name="fk_id_numeric", columnList="id_numeric", unique=false)})
public class NumericSample implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "numer_sam_seq")
	@SequenceGenerator(name = "numer_sam_seq",  sequenceName = "numeric_sample_seq", allocationSize = 1, initialValue = 1)
	private int id_numeric_sample;
	
//	@JoinColumn(name="id_numeric")
//	private int id_numeric;	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_numeric", nullable = false)
	private Numeric numeric;
	
	@Column(name="value")
	private int value;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "source_time")
	private Date source_time;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "device_time")
	private Date device_time;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "presentation_time")
	private Date presentation_time;
	
	
	public NumericSample(){}
	
	public NumericSample(int value){
		this.value = value;
	}


	public int getId_numeric_sample() {
		return id_numeric_sample;
	}


	public void setId_numeric_sample(int id_numeric_sample) {
		this.id_numeric_sample = id_numeric_sample;
	}


//	public int getId_numeric() {
//		return id_numeric;
//	}
//
//
//	public void setId_numeric(int id_numeric) {
//		this.id_numeric = id_numeric;
//	}


	public Date getSource_time() {
		return source_time;
	}


	public void setSource_time(Date source_time) {
		this.source_time = source_time;
	}


	public int getValue() {
		return value;
	}


	public void setValue(int value) {
		this.value = value;
	}


	public Date getDevice_time() {
		return device_time;
	}


	public void setDevice_time(Date device_time) {
		this.device_time = device_time;
	}


	public Date getPresentation_time() {
		return presentation_time;
	}


	public void setPresentation_time(Date presentation_time) {
		this.presentation_time = presentation_time;
	}

	public Numeric getNumeric() {
		return numeric;
	}

	public void setNumeric(Numeric numeric) {
		this.numeric = numeric;
	}
	
	

}
