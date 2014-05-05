/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mb;

import derbyEntities.Customer;
import derbyEntities.DiscountCode;
import derbyEntities.MicroMarket;
import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.transaction.Transactional;

/**
 *
 * @author yoncaBT
 */
@ManagedBean(name = "TutorialMB")
@ViewScoped
public class TutorialMB implements Serializable {

    Integer naptin;
    List<Customer> customerList;
    Customer customerForAdding;
    EntityManagerFactory entityManagerFactory;
    Integer customerId;
    List<DiscountCode> discountCodeList;
    DiscountCode selectedDiscountCode;
    List<MicroMarket> microMarketList;
    MicroMarket selectedMicroMarket;
//    @PersistenceUnit //inject from your application server 
//    EntityManagerFactory emf; 
//    @Resource //inject from your application server 
//    UserTransaction utx; 

    /**
     * Creates a new instance of tutorialMB
     */
    public TutorialMB() {
        EntityManager entityManager = getEntityManagerFactory().createEntityManager();
        Query query = entityManager.createNamedQuery("Customer.findAll");
        customerList = query.getResultList();
//        customerJpaController = new CustomerJpaController(utx,emf);
//        naptin = customerJpaController.getCustomerCount();
        naptin = customerList.size();
        customerId = 1;
        for (Customer customer : customerList) {
            if (customerId < customer.getCustomerId()) {
                customerId = customer.getCustomerId();
            }
        }
        microMarketList = entityManager.createNamedQuery("MicroMarket.findAll").getResultList();
        discountCodeList = entityManager.createNamedQuery("DiscountCode.findAll").getResultList();
        customerId++;
        dialogShowingListener();
    }

    private EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("jsfTutorial2PU");
        }
        return entityManagerFactory;
    }

    public void dialogShowingListener() {
        customerForAdding = new Customer();
        customerForAdding.setCustomerId(customerId++);
    }

    @Transactional
    public void save(Customer customer) {
        EntityManager entityManager = getEntityManagerFactory().createEntityManager();
        customerForAdding.setDiscountCode(selectedDiscountCode);
        customerForAdding.setZip(selectedMicroMarket);
        entityManager.getTransaction().begin();
        entityManager.persist(customerForAdding);
        entityManager.getTransaction().commit();
        customerForAdding = new Customer();
        Query query = entityManager.createNamedQuery("Customer.findAll");
        customerList = query.getResultList();
    }

    public Integer getNaptin() {
        return naptin;
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public Customer getCustomerForAdding() {
        return customerForAdding;
    }

    public void setCustomerForAdding(Customer customerForAdding) {
        this.customerForAdding = customerForAdding;
    }

    public List<DiscountCode> getDiscountCodeList() {
        return discountCodeList;
    }

    public List<MicroMarket> getMicroMarketList() {
        return microMarketList;
    }

    public DiscountCode getSelectedDiscountCode() {
        return selectedDiscountCode;
    }

    public void setSelectedDiscountCode(DiscountCode selectedDiscountCode) {
        this.selectedDiscountCode = selectedDiscountCode;
    }

    public MicroMarket getSelectedMicroMarket() {
        return selectedMicroMarket;
    }

    public void setSelectedMicroMarket(MicroMarket selectedMicroMarket) {
        this.selectedMicroMarket = selectedMicroMarket;
    }

}
