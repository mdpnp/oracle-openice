package org.mdpnp.hiberdds.mappings;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author diego@mdpnp.org
 *
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "NUMERIC")
public class Numeric implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "numer_seq")
	@SequenceGenerator(name = "numer_seq",  sequenceName = "numeric_ora_seq", allocationSize = 1, initialValue = 1)
	private int id_numeric;
	private Object instance_handle;
	
	@Column(name = "unique_device_identifier")
	private String unique_device_identifier;
	@Column(name = "METRIC_ID")
	private String metric_id;
	@Column(name = "VENDOR_METRIC_ID")
	private String vendor_metric_id;
	@Column(name = "INSTANCE_ID")
	private int instance_id;
	@Column(name = "UNIT_ID")
	private String unit_id;
	
	private Set<NumericSample> numericSamples = new HashSet<NumericSample>(0);
	private Set<NumericLifeCycle> numericLifeCyleCol = new HashSet<NumericLifeCycle>(0);
	
	public Numeric(){}
	
	//TODO Add whatever fields are conveninet to create these types
	public Numeric(String unique_device_identifier){
		this.unique_device_identifier = unique_device_identifier;
		
	}

	
	//getters & setters
	public int getId_numeric() {
		return id_numeric;
	}

	public void setId_numeric(int id_numeric) {
		this.id_numeric = id_numeric;
	}

	public Object getInstance_handle() {
		return instance_handle;
	}

	public void setInstance_handle(Object instance_handle) {
		this.instance_handle = instance_handle;
	}

	public String getUnique_device_identifier() {
		return unique_device_identifier;
	}

	public void setUnique_device_identifier(String unique_device_identifier) {
		this.unique_device_identifier = unique_device_identifier;
	}

	public String getMetric_id() {
		return metric_id;
	}

	public void setMetric_id(String metric_id) {
		this.metric_id = metric_id;
	}

	public String getVendor_metric_id() {
		return vendor_metric_id;
	}

	public void setVendor_metric_id(String vendor_metric_id) {
		this.vendor_metric_id = vendor_metric_id;
	}

	public int getInstance_id() {
		return instance_id;
	}

	public void setInstance_id(int instance_id) {
		this.instance_id = instance_id;
	}

	public String getUnit_id() {
		return unit_id;
	}

	public void setUnit_id(String unit_id) {
		this.unit_id = unit_id;
	}
	
	//TODO Complete these annotations based on
	//https://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/collections.html#example.collection.mapping.annotations

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "numeric")
	public Set<NumericSample> getNumericSamples() {
		return numericSamples;
	}

	public void setNumericSamples(Set<NumericSample> numericSamples) {
		this.numericSamples = numericSamples;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "numeric")
	public Set<NumericLifeCycle> getNumericLifeCyleCol() {
		return numericLifeCyleCol;
	}

	public void setNumericLifeCyleCol(Set<NumericLifeCycle> numericLifeCyleCol) {
		this.numericLifeCyleCol = numericLifeCyleCol;
	}
	
	

}
