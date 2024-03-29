/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import controller.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import derbyEntities.ProductCode;
import derbyEntities.Manufacturer;
import derbyEntities.Product;
import derbyEntities.PurchaseOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author yoncaBT
 */
public class ProductJpaController implements Serializable {

    public ProductJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Product product) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (product.getPurchaseOrderCollection() == null) {
            product.setPurchaseOrderCollection(new ArrayList<PurchaseOrder>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            ProductCode productCode = product.getProductCode();
            if (productCode != null) {
                productCode = em.getReference(productCode.getClass(), productCode.getProdCode());
                product.setProductCode(productCode);
            }
            Manufacturer manufacturerId = product.getManufacturerId();
            if (manufacturerId != null) {
                manufacturerId = em.getReference(manufacturerId.getClass(), manufacturerId.getManufacturerId());
                product.setManufacturerId(manufacturerId);
            }
            Collection<PurchaseOrder> attachedPurchaseOrderCollection = new ArrayList<PurchaseOrder>();
            for (PurchaseOrder purchaseOrderCollectionPurchaseOrderToAttach : product.getPurchaseOrderCollection()) {
                purchaseOrderCollectionPurchaseOrderToAttach = em.getReference(purchaseOrderCollectionPurchaseOrderToAttach.getClass(), purchaseOrderCollectionPurchaseOrderToAttach.getOrderNum());
                attachedPurchaseOrderCollection.add(purchaseOrderCollectionPurchaseOrderToAttach);
            }
            product.setPurchaseOrderCollection(attachedPurchaseOrderCollection);
            em.persist(product);
            if (productCode != null) {
                productCode.getProductCollection().add(product);
                productCode = em.merge(productCode);
            }
            if (manufacturerId != null) {
                manufacturerId.getProductCollection().add(product);
                manufacturerId = em.merge(manufacturerId);
            }
            for (PurchaseOrder purchaseOrderCollectionPurchaseOrder : product.getPurchaseOrderCollection()) {
                Product oldProductIdOfPurchaseOrderCollectionPurchaseOrder = purchaseOrderCollectionPurchaseOrder.getProductId();
                purchaseOrderCollectionPurchaseOrder.setProductId(product);
                purchaseOrderCollectionPurchaseOrder = em.merge(purchaseOrderCollectionPurchaseOrder);
                if (oldProductIdOfPurchaseOrderCollectionPurchaseOrder != null) {
                    oldProductIdOfPurchaseOrderCollectionPurchaseOrder.getPurchaseOrderCollection().remove(purchaseOrderCollectionPurchaseOrder);
                    oldProductIdOfPurchaseOrderCollectionPurchaseOrder = em.merge(oldProductIdOfPurchaseOrderCollectionPurchaseOrder);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findProduct(product.getProductId()) != null) {
                throw new PreexistingEntityException("Product " + product + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Product product) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Product persistentProduct = em.find(Product.class, product.getProductId());
            ProductCode productCodeOld = persistentProduct.getProductCode();
            ProductCode productCodeNew = product.getProductCode();
            Manufacturer manufacturerIdOld = persistentProduct.getManufacturerId();
            Manufacturer manufacturerIdNew = product.getManufacturerId();
            Collection<PurchaseOrder> purchaseOrderCollectionOld = persistentProduct.getPurchaseOrderCollection();
            Collection<PurchaseOrder> purchaseOrderCollectionNew = product.getPurchaseOrderCollection();
            List<String> illegalOrphanMessages = null;
            for (PurchaseOrder purchaseOrderCollectionOldPurchaseOrder : purchaseOrderCollectionOld) {
                if (!purchaseOrderCollectionNew.contains(purchaseOrderCollectionOldPurchaseOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PurchaseOrder " + purchaseOrderCollectionOldPurchaseOrder + " since its productId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (productCodeNew != null) {
                productCodeNew = em.getReference(productCodeNew.getClass(), productCodeNew.getProdCode());
                product.setProductCode(productCodeNew);
            }
            if (manufacturerIdNew != null) {
                manufacturerIdNew = em.getReference(manufacturerIdNew.getClass(), manufacturerIdNew.getManufacturerId());
                product.setManufacturerId(manufacturerIdNew);
            }
            Collection<PurchaseOrder> attachedPurchaseOrderCollectionNew = new ArrayList<PurchaseOrder>();
            for (PurchaseOrder purchaseOrderCollectionNewPurchaseOrderToAttach : purchaseOrderCollectionNew) {
                purchaseOrderCollectionNewPurchaseOrderToAttach = em.getReference(purchaseOrderCollectionNewPurchaseOrderToAttach.getClass(), purchaseOrderCollectionNewPurchaseOrderToAttach.getOrderNum());
                attachedPurchaseOrderCollectionNew.add(purchaseOrderCollectionNewPurchaseOrderToAttach);
            }
            purchaseOrderCollectionNew = attachedPurchaseOrderCollectionNew;
            product.setPurchaseOrderCollection(purchaseOrderCollectionNew);
            product = em.merge(product);
            if (productCodeOld != null && !productCodeOld.equals(productCodeNew)) {
                productCodeOld.getProductCollection().remove(product);
                productCodeOld = em.merge(productCodeOld);
            }
            if (productCodeNew != null && !productCodeNew.equals(productCodeOld)) {
                productCodeNew.getProductCollection().add(product);
                productCodeNew = em.merge(productCodeNew);
            }
            if (manufacturerIdOld != null && !manufacturerIdOld.equals(manufacturerIdNew)) {
                manufacturerIdOld.getProductCollection().remove(product);
                manufacturerIdOld = em.merge(manufacturerIdOld);
            }
            if (manufacturerIdNew != null && !manufacturerIdNew.equals(manufacturerIdOld)) {
                manufacturerIdNew.getProductCollection().add(product);
                manufacturerIdNew = em.merge(manufacturerIdNew);
            }
            for (PurchaseOrder purchaseOrderCollectionNewPurchaseOrder : purchaseOrderCollectionNew) {
                if (!purchaseOrderCollectionOld.contains(purchaseOrderCollectionNewPurchaseOrder)) {
                    Product oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder = purchaseOrderCollectionNewPurchaseOrder.getProductId();
                    purchaseOrderCollectionNewPurchaseOrder.setProductId(product);
                    purchaseOrderCollectionNewPurchaseOrder = em.merge(purchaseOrderCollectionNewPurchaseOrder);
                    if (oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder != null && !oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder.equals(product)) {
                        oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder.getPurchaseOrderCollection().remove(purchaseOrderCollectionNewPurchaseOrder);
                        oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder = em.merge(oldProductIdOfPurchaseOrderCollectionNewPurchaseOrder);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = product.getProductId();
                if (findProduct(id) == null) {
                    throw new NonexistentEntityException("The product with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Product product;
            try {
                product = em.getReference(Product.class, id);
                product.getProductId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The product with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<PurchaseOrder> purchaseOrderCollectionOrphanCheck = product.getPurchaseOrderCollection();
            for (PurchaseOrder purchaseOrderCollectionOrphanCheckPurchaseOrder : purchaseOrderCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the PurchaseOrder " + purchaseOrderCollectionOrphanCheckPurchaseOrder + " in its purchaseOrderCollection field has a non-nullable productId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            ProductCode productCode = product.getProductCode();
            if (productCode != null) {
                productCode.getProductCollection().remove(product);
                productCode = em.merge(productCode);
            }
            Manufacturer manufacturerId = product.getManufacturerId();
            if (manufacturerId != null) {
                manufacturerId.getProductCollection().remove(product);
                manufacturerId = em.merge(manufacturerId);
            }
            em.remove(product);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Product> findProductEntities() {
        return findProductEntities(true, -1, -1);
    }

    public List<Product> findProductEntities(int maxResults, int firstResult) {
        return findProductEntities(false, maxResults, firstResult);
    }

    private List<Product> findProductEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Product.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Product findProduct(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Product> rt = cq.from(Product.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
