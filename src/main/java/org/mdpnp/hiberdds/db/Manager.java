package org.mdpnp.hiberdds.db;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mdpnp.hiberdds.mappings.VitalValues;
import org.mdpnp.hiberdds.mappings.VitalValuesDTO;
import org.mdpnp.hiberdds.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manage CRUD operations to the DB
 * @author diego@mdpnp.org
 *
 */
public class Manager {
	
	private Logger logger = LoggerFactory.getLogger(Manager.class);

	public VitalValuesDTO createVitalValues(VitalValuesDTO vitalValuesDto){		
		Session session = null;
		Transaction tx = null;
		Integer vitalID = null;	
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			vitalID = (Integer)session.save(vitalValuesDto); //cast to Long?
			tx.commit();
			
		}catch(HibernateException e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			vitalID = null;
		}catch(Exception e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			vitalID = null;
		}finally{
			if(null != session && session.isOpen()){
				session.close();
			}
		}
		if(null!=vitalID)
			vitalValuesDto.setId_vital_values(vitalID);
		return vitalValuesDto;
	}
	
	public VitalValues createVitalValuesORA(VitalValues vitalValues){		
		Session session = null;
		Transaction tx = null;
		Integer vitalID = null;	
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			vitalID = (Integer)session.save(vitalValues); //cast to Long?
			tx.commit();
			
		}catch(HibernateException e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			vitalID = null;
		}catch(Exception e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			vitalID = null;
		}finally{
			if(null != session && session.isOpen()){
				session.close();
			}
		}
		vitalValues.setId_vital_values(vitalID);
		return vitalValues;
	}
	
	public VitalValuesDTO saveOrUpdate_VitalValues(VitalValuesDTO vitalValuesDto){		
		Session session = null;
		Transaction tx = null;
		Integer vitalID = null;	
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			session.saveOrUpdate(vitalValuesDto);
			vitalID = vitalValuesDto.getId_vital_values();
			tx.commit();
			
		}catch(HibernateException e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			vitalID = null;
		}catch(Exception e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			vitalID = null;
		}finally{
			if(null != session && session.isOpen()){
				session.close();
			}
		}
		vitalValuesDto.setId_vital_values(vitalID);
		return vitalValuesDto;
	}
	
	public boolean deleteVitalValues(VitalValuesDTO vitalValuesDto){
		Session session = null;
		Transaction tx = null;
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			session.delete(vitalValuesDto);
			tx.commit();
			return true;
		}catch(HibernateException e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			return false;
		}catch(Exception e){
			if(null != tx){
				tx.rollback();
			}
			logger.error(e.getMessage());
			return false;
		}finally{
			if(null != session && session.isOpen()){
				session.close();
			}
		}
	}
}
