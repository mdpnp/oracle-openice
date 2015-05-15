package org.mdpnp.hiberdds.testapp;

import java.util.Date;

import org.hibernate.Session;
import org.mdpnp.hiberdds.db.Manager;
import org.mdpnp.hiberdds.mappings.VitalValues;
import org.mdpnp.hiberdds.mappings.VitalValuesDTO;
import org.mdpnp.hiberdds.util.HibernateUtil;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//test_w_annotations();
		//test_ValueORA_mappingfile();
		
	    Session session =  HibernateUtil.getSessionFactory().openSession();
	    session.beginTransaction();
	    VitalValues vv = new VitalValues("test_FIVOQQm6kjMY6arhvxJZ7CwzEX24BSi", "MDC_PULS_OXIM_PULS_RATE", 0, new Date(), 60);
	    session.save(vv);
	    session.getTransaction().commit();
	    session.close();
	    HibernateUtil.getSessionFactory().close();


	}

	
	private static void test_w_annotations(){
		VitalValues vv = new VitalValues("test_FIVOQQm6kjMY6arhvxJZ7CwzEX24BSi", "MDC_PULS_OXIM_PULS_RATE", 0, new Date(), 0);
		Manager mng = new Manager();
		vv = mng.createVitalValuesORA(vv);
		
		if(HibernateUtil.getSession().isOpen()){
			HibernateUtil.getSessionFactory().close();
			System.out.println("closed sessions");
		}
		
		System.out.println("generated ID "+vv.getId_vital_values());
		System.out.println("done");
	}
	
	
	private static void test_ValueORA_mappingfile(){
		VitalValuesDTO vitalValDto = new VitalValuesDTO();
		vitalValDto.setDevice_id("test_FIVOQQm6kjMY6arhvxJZ7CwzEX24BSi");
		vitalValDto.setMetric_id("MDC_PULS_OXIM_PULS_RATE");
		vitalValDto.setInstance_id(0);
		vitalValDto.setTime_tick(new Date());
		vitalValDto.setVital_value(60);
		
		Manager mng = new Manager();
		vitalValDto = mng.createVitalValues(vitalValDto);
		
		if(HibernateUtil.getSession().isOpen()){
			HibernateUtil.getSessionFactory().close();
			System.out.println("closed sessions");
		}
		
		System.out.println("generated ID "+vitalValDto.getId_vital_values());
		System.out.println("done");
	}
}
