package org.mdpnp.hiberdds.mappings;

import java.util.Date;

/**
 * Class to map the values to table jhopkins.VITAL_VALUES
 * 
 * This class is not annotated, so is linked in the hibernate.cfg.xml
 * configuration file
 * 
 * @author diego@mdpnp.org
 *
 */
public class VitalValuesDTO {
	
	private int id_vital_values; 
	private String device_id;
	private String metric_id;
	private int instance_id;
	private Date time_tick;
	private int vital_value;

	
	public VitalValuesDTO(){}
	
	public VitalValuesDTO(String deviceID, String metricID, int instanceID, Date timeTick, int value){
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
