package org.mdpnp.hiberdds.mappings;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Class to map the values to table VITAL_VALUES
 * @author diego@mdpnp.org
 * 
 * NOTE: 
 * 
 * As per https://docs.jboss.org/hibernate/orm/3.6/quickstart/en-US/html/hibernate-gsg-tutorial-annotations.html
 * This should be the right way to annotate for everyone except Oracle, which is going to need its own sequence 
 * @Id
 * @GeneratedValue(generator="increment")
 * @GenericGenerator(name="increment", strategy = "increment")
 *   public Long getId() {
 *     return id;
 *  }
 *
 */
@Entity
@Table(name = "VITAL_VALUES_ORA")
public class VitalValues {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vital_seq")
	@SequenceGenerator(name = "vital_seq",  sequenceName = "vital_values_ora_seq", allocationSize = 1, initialValue = 1)
	private int id_vital_values; 
	
	@Column(name = "DEVICE_ID")
	private String device_id;
	
	@Column(name = "METRIC_ID")
	private String metric_id;
	
	@Column(name = "INSTANCE_ID")
	private int instance_id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIME_TICK")
	private Date time_tick;
	
	@Column(name = "VITAL_VALUE")
	private int vital_value;
	
	public VitalValues(){}
	
	public VitalValues(String deviceID, String metricID, int instanceID, Date timeTick, int value){
		this.device_id = deviceID;
		this.metric_id = metricID;
		this.instance_id = instanceID;
		this.time_tick = timeTick;
		this.vital_value = value;
	}


	public int getId_vital_values() {
		return id_vital_values;
	}

	public void setId_vital_values(int id_vital_values) {
		this.id_vital_values = id_vital_values;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}


	public String getMetric_id() {
		return metric_id;
	}

	public void setMetric_id(String metric_id) {
		this.metric_id = metric_id;
	}

	
	public int getInstance_id() {
		return instance_id;
	}

	public void setInstance_id(int instance_id) {
		this.instance_id = instance_id;
	}


	public Date getTime_tick() {
		return time_tick;
	}

	public void setTime_tick(Date time_tick) {
		this.time_tick = time_tick;
	}

	
	public int getVital_value() {
		return vital_value;
	}

	public void setVital_value(int vital_value) {
		this.vital_value = vital_value;
	}
	
	
	
	
}
