package org.mdpnp.hiberdds.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom class created as a helper utility, to retrieve and use the Hibernate session factory
 *
 */
public class HibernateUtil {

	  private static final SessionFactory sessionFactory;
	  private static final ServiceRegistry serviceRegistry;
	  private static Logger loggerHibernateUtil = LoggerFactory.getLogger(HibernateUtil.class); 

	  static {
	    try {
	    	//XXX this changes from Hibernate 3.X to Hibernate 4.X
	    	//check http://stackoverflow.com/questions/8621906/is-buildsessionfactory-deprecated-in-hibernate-4
	    	//if malfunctions
	    	
	      // Create the SessionFactory from hibernate.cfg.xml
//	      sessionFactory = new Configuration().configure().buildSessionFactory(); // deprecated from Hibernate 3.0 to 4.0
//	      sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
//	      SessionFactory sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
//	      session=sessionFactory.openSession();
	      
	      // Create the SessionFactory from hibernate.cfg.xml
		    Configuration configuration = new Configuration();
		    configuration.configure();
		    serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		    sessionFactory = configuration.buildSessionFactory(serviceRegistry);

	    } catch (Throwable ex) {
	      // Make sure you log the exception, as it might be swallowed
	      System.err.println("Initial SessionFactory creation failed." + ex);
	      loggerHibernateUtil.error("Initial SessionFactory creation failed." + ex);
	      throw new ExceptionInInitializerError(ex);
	    }
	  }

	  public static SessionFactory getSessionFactory() {
	    return sessionFactory;
	  }
	  
	  /**
	   * Returns the Hibernate current session or opnes a new one if needed <p>
	   * A hibernate session is tied to a specific thread and closed on commit/rollback, 
	   * but the session factory's getCurrentSession() method gets a new session as needed,
	   * BUT session is not thread safe.
	   * @return
	   */
	  public static Session getSession() {
		   return sessionFactory.getCurrentSession();
	  }
}
