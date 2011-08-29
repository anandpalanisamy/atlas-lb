package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entities.*;

import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.DELETED;

@Repository
@Transactional
public class LoadBalancerRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;/**/


    public LoadBalancer getById(Integer id) throws EntityNotFoundException {
        LoadBalancer lb = entityManager.find(LoadBalancer.class, id);
        if (lb == null) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }
        return lb;
    }

    public UserPages getUserPages(Integer lid, Integer aid) {
        List<UserPages> userPagesList = new ArrayList<UserPages>();
        UserPages up;
        String qStr = "FROM UserPages u where u.loadbalancer.id = :lid and u.loadbalancer.accountId = :aid";
        Query q = entityManager.createQuery(qStr).setParameter("lid", lid).setParameter("aid", aid);
        userPagesList = q.setMaxResults(1).getResultList();
        if (userPagesList.size() <= 0) {
            up = null;
        } else {
            up = userPagesList.get(0);
        }
        return up;
    }

    public Defaults getDefaultErrorPage() {
        List<Defaults> defaultsList = new ArrayList<Defaults>();
        Defaults up;
        String qStr = "FROM Defaults d WHERE d.name = :globalError";
        Query q = entityManager.createQuery(qStr).setParameter("globalError", "global_error.html");
        defaultsList = q.setMaxResults(1).getResultList();
        if (defaultsList.size() <= 0) {
            up = null;
        } else {
            up = defaultsList.get(0);
        }
        return up;
    }

    public String getErrorPage(Integer lid, Integer aid) throws EntityNotFoundException {
        UserPages up;
        up = getUserPages(lid, aid);
        if (up == null) {
            return null;
        }
        return up.getErrorpage();
    }

    public boolean setErrorPage(Integer lid,Integer aid,String errorpage) throws EntityNotFoundException{
        boolean out=false;
        LoadBalancer lb = getByIdAndAccountId(lid, aid);
        UserPages up = getUserPages(lid,aid);
        if(up==null){
            up = new UserPages();
            up.setLoadbalancer(lb);
            up.setErrorpage(errorpage);
            entityManager.merge(up);
            return true;
        }else{
            up.setErrorpage(errorpage);
            entityManager.merge(up);
            return false;
        }
    }

    public boolean setDefaultErrorPage(String errorpage) throws EntityNotFoundException{
        boolean out=false;
        Defaults up = getDefaultErrorPage();
        if(up==null){
            up = new Defaults();
            up.setName(Constants.DEFAULT_ERRORFILE);
            up.setValue(errorpage);
            entityManager.merge(up);
            return true;
        }else{
            up.setValue(errorpage);
            entityManager.merge(up);
            return false;
        }
    }

    public boolean removeErrorPage(Integer lid, Integer aid) {
        UserPages up = getUserPages(lid, aid);
        if (up == null) {
            return false;
        } else if (up.getErrorpage() == null) {
            return false;
        }
        up.setErrorpage(null);
        entityManager.merge(up);
        return true;


    }

    public LoadBalancer getByIdAndAccountId(Integer id, Integer accountId) throws EntityNotFoundException {
        LoadBalancer lb;
        lb = getById(id);
        if (!lb.getAccountId().equals(accountId)) {
            String message = Constants.LoadBalancerNotFound;
            LOG.warn(message);
            throw new EntityNotFoundException(message);
        }

        return lb;
    }

    public List<LoadBalancer> getExpiredLbs() {
        Calendar threeMonthsAgo = Calendar.getInstance();
        threeMonthsAgo.add(Calendar.DATE, -90);
        String queryStr = "select l from LoadBalancer l where l.status = :status and l.updated <= :threeMonthsAgo";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter("threeMonthsAgo", threeMonthsAgo);
        query.setParameter("status", LoadBalancerStatus.DELETED);
        List results = query.getResultList();

        return results;
    }

    public void removeExpiredLb(int lbId) {
        LoadBalancer lb = entityManager.find(LoadBalancer.class, lbId);
        entityManager.remove(lb);
    }

    public AccessList getNetworkItemByAccountIdLoadBalancerIdNetworkItemId(Integer aid, Integer lid, Integer nid) throws EntityNotFoundException {
        List<AccessList> al = null;
        String qStr = "SELECT a from AccessList a "
                + "        WHERE a.loadbalancer.id = :lid "
                + "          and a.loadbalancer.accountId = :aid "
                + "          and a.id = :nid";
        // Don't put up with the possibility of bogus querys
        if (lid == null || aid == null || nid == null) {
            throw new EntityNotFoundException("Null parameter Query rejected");
        }

        Query q = entityManager.createQuery(qStr);
        q.setParameter("aid", aid);
        q.setParameter("lid", lid);
        q.setParameter("nid", nid);
        q.setMaxResults(1);
        al = q.getResultList();
        if (al.size() != 1) {
            throw new EntityNotFoundException("Node not nound");
        }
        return al.get(0);
    }

    public Integer getNumNonDeletedLoadBalancersForAccount(Integer accountId) {
        Query query = entityManager.createNativeQuery(
                "select count(account_id) from loadbalancer where status != 'DELETED' and account_id = :accountId").setParameter("accountId", accountId);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public List<LoadBalancerProtocolObject> getAllProtocols() {
        List<LoadBalancerProtocolObject> protocolObjects;
        protocolObjects = entityManager.createQuery(
                "from LoadBalancerProtocolObject where enabled = True").getResultList();
        if (protocolObjects.isEmpty()) {
            protocolObjects = new ArrayList<LoadBalancerProtocolObject>();
        }
        return protocolObjects;
    }

    public List<LoadBalancerAlgorithmObject> getAllAlgorithms() {
        List<LoadBalancerAlgorithmObject> algorithmObjects;
        algorithmObjects = entityManager.createQuery(
                "from LoadBalancerAlgorithmObject lba where lba.enabled = True").getResultList();
        if (algorithmObjects.isEmpty()) {
            algorithmObjects = new ArrayList<LoadBalancerAlgorithmObject>();
        }
        return algorithmObjects;
    }

    public ConnectionLimit getConnectionLimitsbyAccountIdLoadBalancerId(Integer accountId,
            Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException {
        LoadBalancer lb = getByIdAndAccountId(loadbalancerId, accountId);
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        if (lb.getConnectionLimit() == null) {
            return new ConnectionLimit();
        }
        return lb.getConnectionLimit();
    }

    public RateLimit getRateLimitByLoadBalancerId(
            Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException {
        LoadBalancer lb = getById(loadbalancerId);
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        if (lb.getRateLimit() == null) {
            throw new EntityNotFoundException("No rate limit exists");
        }
        return lb.getRateLimit();
    }

    public SessionPersistence getSessionPersistenceByAccountIdLoadBalancerId(Integer accountId,
            Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException, BadRequestException {
        LoadBalancer lb = getByIdAndAccountId(loadbalancerId, accountId);
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        if (lb.getSessionPersistence() == null) {
            throw new EntityNotFoundException("No session persistence exists");
        }
        return lb.getSessionPersistence();
    }

    public LoadBalancer enableSessionPersistenceByIdAndAccountId(Integer id,
            Integer accountId) throws EntityNotFoundException, BadRequestException {
        LoadBalancer lb;
        lb = getById(id);
        if (!lb.getAccountId().equals(accountId)) {
            throw new EntityNotFoundException(String.format("Load balancer not found"));
        }
        if (!lb.getProtocol().equals(LoadBalancerProtocol.HTTP)) {
            throw new BadRequestException(
                    "Bad Request: The requirements were not met for this request, please verify with spec and try again.");
        }

        return lb;
    }

    public boolean testAndSetStatus(Integer accountId, Integer loadbalancerId, LoadBalancerStatus statusToChangeTo, boolean allowConcurrentModifications) throws EntityNotFoundException, UnprocessableEntityException {
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException();
        }

        LoadBalancer lb = lbList.get(0);
        if (lb.getStatus().equals(DELETED)) {
            throw new UnprocessableEntityException(Constants.LoadBalancerDeleted);
        }
        final boolean isActive = lb.getStatus().equals(LoadBalancerStatus.ACTIVE);
        final boolean isPendingOrActive = lb.getStatus().equals(LoadBalancerStatus.PENDING_UPDATE) || isActive;

        if (allowConcurrentModifications ? isPendingOrActive : isActive) {
            lb.setStatus(statusToChangeTo);
            lb.setUpdated(Calendar.getInstance());
            entityManager.merge(lb);
            return true;
        }

        return false;
    }

    public List<Usage> getUsageByAccountIdandLbId(Integer accountId, Integer loadBalancerId, Calendar startTime, Calendar endTime) throws EntityNotFoundException, DeletedStatusException {
        // TODO: Find more efficient way of making sure loadbalancer exists
        getByIdAndAccountId(loadBalancerId, accountId); // Make sure loadbalancer exists
        return getUsageByLbId(loadBalancerId, startTime, endTime);
    }

    public void setStatus(Integer accountId, Integer loadbalancerId, LoadBalancerStatus status) throws EntityNotFoundException {
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException();
        }


        lbList.get(0).setStatus(status);
        entityManager.merge(lbList.get(0));
    }

    public boolean testAndSetStatusPending(Integer accountId, Integer loadbalancerId) throws EntityNotFoundException {
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException();
        }
        if (!lbList.get(0).getStatus().equals(LoadBalancerStatus.ACTIVE)) {
            return false;
        }

        lbList.get(0).setStatus(LoadBalancerStatus.PENDING_UPDATE);
        lbList.get(0).setUpdated(Calendar.getInstance());
        entityManager.merge(lbList.get(0));
        return true;
    }

    public List<LoadBalancer> getNonDeletedByAccountId(Integer accountId) {
        List<LoadBalancer> lbs = new ArrayList<LoadBalancer>();
        String qStr = "select lb from LoadBalancer lb where status !='DELETED' and lb.accountId = :accountId";
        Query q = entityManager.createQuery(qStr).setParameter("accountId", accountId);
        lbs = q.getResultList();
        return lbs;
    }

    /**
     * This method is optimized performance and only pulls the information that is required for displaying the loadbalancers list, instead
     * of eager-loading the childrens.
     */
    public List<LoadBalancer> getNonDeletedByAccountId(Integer accountId, Integer... p) {
        List<LoadBalancer> lbs = new ArrayList<LoadBalancer>();
        LoadBalancerStatus lbStatus = LoadBalancerStatus.DELETED;

        Query query;
        String qStr = "SELECT lb.id, lb.accountId, lb.name, lb.algorithm, lb.protocol, lb.port, lb.status, lb.created, lb.updated  FROM LoadBalancer lb WHERE lb.accountId = :accountId and lb.status != :status";

        query = entityManager.createQuery(qStr).setParameter("accountId", accountId).setParameter("status", lbStatus);


        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            Integer changesSince = p[2];
            Integer marker = p[3];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null) {
                limit = 100;
            }
            if (marker != null) {

                if (lbStatus != null) {
                    query = entityManager.createQuery(
                            "SELECT lb.id, lb.accountId, lb.name, lb.algorithm, lb.protocol, lb.port, lb.status, lb.created, lb.updated FROM LoadBalancer lb WHERE lb.accountId = :accountId and lb.id >= :lbId and lb.status != :status").setParameter("accountId", accountId).setParameter("lbId", marker).setParameter("status", lbStatus);
                }
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        List<Object[]> rows = query.getResultList();
        for (Object[] obj : rows) {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId((Integer) obj[0]);
            loadBalancer.setAccountId((Integer) obj[1]);
            loadBalancer.setName((String) obj[2]);
            loadBalancer.setAlgorithm((LoadBalancerAlgorithm) obj[3]);
            loadBalancer.setProtocol((LoadBalancerProtocol) obj[4]);
            loadBalancer.setPort((Integer) obj[5]);
            loadBalancer.setStatus((LoadBalancerStatus) obj[6]);
            loadBalancer.setCreated((Calendar) obj[7]);
            loadBalancer.setUpdated((Calendar) obj[8]);
            List<LoadBalancerJoinVip> vips = getVipsByLoadBalancerId(loadBalancer.getId());
            loadBalancer.setLoadBalancerJoinVipSet(new HashSet(vips));
            lbs.add(loadBalancer);
        }
        return lbs;
    }

    public List<LoadBalancerJoinVip> getVipsByLoadBalancerId(Integer loadBalancerId) {
        List<LoadBalancerJoinVip> vips;
        String query = "select j from LoadBalancerJoinVip j where j.loadBalancer.id = :loadBalancerId";
        //String query = "select l.virtualIps from LoadBalancer l where l.id = :loadBalancerId";
        vips = entityManager.createQuery(query).setParameter("loadBalancerId", loadBalancerId).getResultList();
        return vips;
    }

    public List<LoadBalancer> getLoadbalancersGeneric(Integer accountId,
            String status, LbQueryStatus queryStatus, Calendar changedSince,
            Integer offset, Integer limit, Integer marker) throws BadRequestException {
        List<LoadBalancer> lbs = new ArrayList<LoadBalancer>();
        LoadBalancerStatus lbStatus;
        String selectClause;
        String format;
        CustomQuery cq;
        String msg;
        selectClause = "SELECT lb FROM LoadBalancer lb";
        String op;
        String qStr;
        Query q;
        cq = new CustomQuery(selectClause);
//        cq.addOrderdBy("lb.id", "desc");

        if (accountId != null) {
            cq.addParam("lb.accountId", "=", "accountId", accountId);
        }

        if (queryStatus != null && status != null) {
            try {
                lbStatus = LoadBalancerStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                msg = String.format("%s is not a valid LoadBalancer status", status);
                throw new BadRequestException(msg);
            }
            switch (queryStatus) {
                case EXCLUDE:
                    op = "!=";
                    break;
                case INCLUDE:
                    op = "=";
                    break;
                default:
                    format = "QueryStatus %s must be INCLUDE or EXCLUDE or null";
                    msg = String.format(format, queryStatus.name());
                    throw new BadRequestException(msg);
            }
            cq.addParam("lb.status", op, "status", lbStatus);
        }

        if (changedSince != null) {
            cq.addParam("lb.updated", ">=", "updated", changedSince);
        }

        if (marker != null) {
            cq.addParam("lb.id", ">=", "marker", marker);
        }

        qStr = cq.getQueryString();

        q = entityManager.createQuery(qStr);

        for (QueryParameter param : cq.getQueryParameters()) {
            q.setParameter(param.getPname(), param.getValue());
        }

        if (limit != null) {
            cq.setLimit(limit);
            q.setMaxResults(cq.getLimit());
        }

        if (offset != null) {
            cq.setOffset(offset);
            q.setFirstResult(cq.getOffset());
        }

        lbs = q.getResultList();

        return lbs;
    }

    public List<LoadBalancer> getByAccountId(Integer accountId, String status, Integer... p) {
        List<LoadBalancer> loadbalancers = new ArrayList<LoadBalancer>();
        LoadBalancerStatus lbStatus = null;
        if (status != null) {
            try {
                lbStatus = LoadBalancerStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                lbStatus = null;
            }
        }


        Query query;
        query = entityManager.createQuery("SELECT lb FROM LoadBalancer lb WHERE lb.accountId = :accountId").setParameter("accountId", accountId);

        if (lbStatus != null) {
            query = entityManager.createQuery("SELECT lb FROM LoadBalancer lb WHERE lb.accountId = :accountId and lb.status = :status").setParameter("accountId", accountId).setParameter("status", lbStatus);
        }

        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            Integer changesSince = p[2];
            Integer marker = p[3];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null) {
                limit = 100;
            }
            if (changesSince != null) {
                List<LoadBalancer> changesLb = getChangesSince(accountId, status, changesSince, offset, limit);
                return changesLb;
            }
            if (marker != null) {
                query = entityManager.createQuery(
                        "SELECT lb FROM LoadBalancer lb WHERE lb.accountId = :accountId and lb.id >= :lbId").setParameter("accountId", accountId).setParameter("lbId", marker);

                if (lbStatus != null) {
                    query = entityManager.createQuery(
                            "SELECT lb FROM LoadBalancer lb WHERE lb.accountId = :accountId and lb.id >= :lbId and lb.status = :status").setParameter("accountId", accountId).setParameter("lbId", marker).setParameter("status", lbStatus);
                }
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        loadbalancers = query.getResultList();
        return loadbalancers;
    }

    // For Jira https://jira.mosso.com/browse/SITESLB-220
    public List<AccountLoadBalancer> getAccountLoadBalancers(
            int accountId) { // Jira: https://jira.mosso.com/browse/SITESLB-220
        List<AccountLoadBalancer> accountLoadBalancers = new ArrayList<AccountLoadBalancer>();
        List<Object> hResults;
        String queryStr = "select l.id, "
                + "l.name, "
                + "c.id, "
                + "c.name, "
                + "l.status, "
                + "l.protocol "
                + "from LoadBalancer l join l.host h join h.cluster c "
                + "where l.accountId=:accountId";
        hResults = entityManager.createQuery(queryStr).setParameter("accountId", accountId).getResultList();
        for (Object row : hResults) {
            Object[] t = (Object[]) row;
            LoadBalancerStatus status;
            LoadBalancerProtocol protocol;
            AccountLoadBalancer accountLoadBalancer = new AccountLoadBalancer();
            accountLoadBalancer.setLoadBalancerId((Integer) t[0]);
            accountLoadBalancer.setLoadBalancerName((String) t[1]);
            accountLoadBalancer.setClusterId((Integer) t[2]);
            accountLoadBalancer.setClusterName((String) t[3]);
            status = (LoadBalancerStatus) t[4];
            accountLoadBalancer.setStatus(status.toString());
            protocol = (LoadBalancerProtocol) t[5];
            accountLoadBalancer.setProtocol(protocol.toString());
            accountLoadBalancers.add(accountLoadBalancer);
        }
        return accountLoadBalancers;
    }

    public List<AccessList> getAccessListByAccountIdLoadBalancerId(int accountId, int loadbalancerId,
            Integer... p) throws EntityNotFoundException, DeletedStatusException {
        LoadBalancer lb = getByIdAndAccountId(loadbalancerId,
                accountId); // Puke if the LoadBalancer is not found presumebly Account LoadBalancer mismatch
        List<AccessList> accessList = new ArrayList<AccessList>();
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        Query query = entityManager.createQuery(
                "from AccessList a where a.loadbalancer.id = :lid and a.loadbalancer.accountId = :aid").setParameter(
                "lid", loadbalancerId).setParameter("aid", accountId);

        if (p.length >= 3) {
            Integer offset = p[0];
            Integer limit = p[1];
            Integer marker = p[2];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker != null) {
                query = entityManager.createQuery(
                        "from AccessList a where a.loadbalancer.id = :lid and a.loadbalancer.accountId = :aid and a.id >= :accessId").setParameter(
                        "lid", loadbalancerId).setParameter("aid", accountId).setParameter("accessId", marker);
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        accessList = query.getResultList();
        return accessList;

    }

    public HealthMonitor getHealthMonitor(Integer accountId,
            Integer loadbalancerId) throws EntityNotFoundException, DeletedStatusException {
        LoadBalancer lb = getByIdAndAccountId(loadbalancerId, accountId);
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        if (lb.getHealthMonitor() == null) {
            return new HealthMonitor();
        }
        return lb.getHealthMonitor();
    }

    public Set<VirtualIp> getVipsByAccountIdLoadBalancerId(Integer accountId, Integer loadBalancerId,
            Integer... p) throws EntityNotFoundException, DeletedStatusException {
        LoadBalancer lb = getByIdAndAccountId(loadBalancerId, accountId);
        if (lb.getStatus().equals(LoadBalancerStatus.DELETED)) {
            throw new DeletedStatusException("The loadbalancer is marked as deleted.");
        }
        if (lb.getLoadBalancerJoinVipSet() == null || lb.getLoadBalancerJoinVipSet().isEmpty()) {
            throw new EntityNotFoundException("No virtual ips associated with the loadbalancer");
        }
        Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = lb.getLoadBalancerJoinVipSet();
        Set<VirtualIp> vips_out = new LinkedHashSet<VirtualIp>();

        if (p.length >= 2) {
            Integer offset = p[0];
            Integer limit = p[1];
            Integer marker = p[2];
            int i = 0;
            if (offset == null) {
                offset = 0;
            }
            if (limit == null || limit > 100) {
                limit = 100;
            }
            if (marker == null) {
                marker = 0;
            }
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancerJoinVipSet) {
                i++;
                if (loadBalancerJoinVip.getVirtualIp().getId() >= marker) {
                    if (i >= offset) {
                        vips_out.add(loadBalancerJoinVip.getVirtualIp());
                    }
                    if (i >= offset + limit) {
                        break;
                    }
                }
            }
        } else {
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancerJoinVipSet) {
                vips_out.add(loadBalancerJoinVip.getVirtualIp());
            }
        }
        return vips_out;
    }

    private void paginateLists(Object obj, Integer marker, Integer limit) {
        if (obj instanceof LoadBalancer) {
            ((LoadBalancer) obj).getId();
        }
    }

    private void setLbIdOnChildObjects(LoadBalancer loadBalancer) {
        if (loadBalancer.getNodes() != null) {
            for (Node node : loadBalancer.getNodes()) {
                node.setLoadbalancer(loadBalancer);
            }
        }

        if (loadBalancer.getAccessLists() != null) {
            for (AccessList accessList : loadBalancer.getAccessLists()) {
                accessList.setLoadbalancer(loadBalancer);
            }
        }

        if (loadBalancer.getConnectionLimit() != null) {
            loadBalancer.getConnectionLimit().setLoadBalancer(loadBalancer);
        }
        if (loadBalancer.getHealthMonitor() != null) {
            loadBalancer.getHealthMonitor().setLoadbalancer(loadBalancer);
        }
    }

    public LoadBalancer update(LoadBalancer loadBalancer) {
        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            VirtualIp virtualIp = entityManager.find(VirtualIp.class, lbJoinVipToLink.getVirtualIp().getId());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            entityManager.merge(loadBalancerJoinVip);
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
        }

        entityManager.flush();
        return loadBalancer;
    }

    public LoadBalancer setStatus(LoadBalancer loadBalancer, LoadBalancerStatus status) throws EntityNotFoundException {
        loadBalancer = getById(loadBalancer.getId());
        loadBalancer.setStatus(status);
        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);
        entityManager.flush();
        return loadBalancer;
    }

    public LoadBalancer create(LoadBalancer loadBalancer) {
        Calendar formattedCal = dateFormatter();

        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);
        final Set<LoadBalancerJoinVip6> lbJoinVip6sToLink = loadBalancer.getLoadBalancerJoinVip6Set();
        loadBalancer.setLoadBalancerJoinVip6Set(null);

        setLbIdOnChildObjects(loadBalancer);

        loadBalancer.setCreated(Calendar.getInstance());
        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, lbJoinVipToLink.getVirtualIp());
            entityManager.merge(loadBalancerJoinVip);
        }

        loadBalancer.setLoadBalancerJoinVip6Set(lbJoinVip6sToLink);

        entityManager.flush();
        return loadBalancer;
    }

    public void persist(Object obj) {
        entityManager.persist(obj);
    }

    public Calendar dateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ssZ");
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        String formattedDate = formatter.format(now);
        Date dateProc = null;
        try {
            dateProc = formatter.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.setTime(dateProc);
        return cal;
    }

    public LoadBalancerProtocolObject getDefaultProtocol() {
        //TODO: Add enable/disable logic
        return (LoadBalancerProtocolObject) entityManager.createQuery(
                "SELECT p FROM LoadBalancerProtocolObject p WHERE name = 'HTTP'").getSingleResult();
    }

    public void delete(Object o) {
        entityManager.remove(o);
        entityManager.flush();
    }

    public Object save(Object o) {
        entityManager.persist(o);
        entityManager.flush();
        return o;
    }

    public ConnectionLimit createConnectionLimit(LoadBalancer loadBalancer, ConnectionLimit connectionLimit) {
        connectionLimit.setLoadBalancer(loadBalancer);
        connectionLimit = entityManager.merge(connectionLimit);
        return connectionLimit;
    }

    public ConnectionLimit updateConnectionLimit(LoadBalancer loadBalancer, ConnectionLimit connectionLimit) {
        connectionLimit.setId(loadBalancer.getConnectionLimit().getId());
        connectionLimit.setLoadBalancer(loadBalancer);
        connectionLimit = entityManager.merge(connectionLimit);
        return connectionLimit;
    }

    public RateLimit createRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) {
        createTicket(loadBalancer, rateLimit.getTicket());
        rateLimit.setLoadbalancer(loadBalancer);
        rateLimit = entityManager.merge(rateLimit);
        return rateLimit;
    }

    public RateLimit updateRateLimit(LoadBalancer loadBalancer, RateLimit rateLimit) {
        rateLimit.setId(loadBalancer.getRateLimit().getId());
        rateLimit.setLoadbalancer(loadBalancer);
        rateLimit = entityManager.merge(rateLimit);
        return rateLimit;
    }

    public Ticket createTicket(LoadBalancer loadBalancer, Ticket ticket) {
        ticket.setLoadbalancer(loadBalancer);
        ticket = entityManager.merge(ticket);
        return ticket;
    }

    public void removeRateLimit(LoadBalancer loadBalancer) {
        RateLimit rateLimit = loadBalancer.getRateLimit();
        rateLimit = entityManager.merge(rateLimit);
        entityManager.remove(rateLimit);
    }

    public HealthMonitor createHealthMonitor(LoadBalancer loadBalancer, HealthMonitor healthMonitor) {
        healthMonitor.setLoadbalancer(loadBalancer);
        healthMonitor = entityManager.merge(healthMonitor);
        return healthMonitor;
    }

    public HealthMonitor updateHealthMonitor(LoadBalancer loadBalancer, HealthMonitor healthMonitor) {
        healthMonitor.setId(loadBalancer.getHealthMonitor().getId());
        healthMonitor.setLoadbalancer(loadBalancer);
        healthMonitor = entityManager.merge(healthMonitor);
        return healthMonitor;
    }

    public void removeHealthMonitor(LoadBalancer loadBalancer) {
        HealthMonitor monitor = loadBalancer.getHealthMonitor();
        monitor = entityManager.merge(monitor);
        entityManager.remove(monitor);
    }

    public void removeConnectionThrottle(LoadBalancer loadBalancer) {
        ConnectionLimit throttle = loadBalancer.getConnectionLimit();
        throttle = entityManager.merge(throttle);
        entityManager.remove(throttle);
    }

    // TODO: Delete me before prod
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public Suspension getSuspensionByLbIdAndAccountId(int loadbalancerId) throws EntityNotFoundException {
        List<Suspension> sList;
        sList = entityManager.createQuery("from Suspension s where s.loadbalancer.id = :lid").setParameter("lid",
                loadbalancerId).getResultList();
        //we want to return an empty list element if this list is empty...
        if (sList.isEmpty()) {
            return new Suspension();
        } else {
            return sList.get(0);
        }
    }

    public Suspension createSuspension(LoadBalancer loadBalancer, Suspension suspension) {
        createTicket(loadBalancer, suspension.getTicket());
        suspension.setLoadbalancer(loadBalancer);
        suspension = entityManager.merge(suspension);
        return suspension;
    }

    public void removeSuspension(int loadbalancerId) {
        entityManager.createQuery("delete from Suspension s where s.loadbalancer.id = :lid").setParameter("lid",
                loadbalancerId).executeUpdate();
    }

    public List<Usage> getUsageByLbId(Integer loadBalancerId, Calendar startTime, Calendar endTime) throws EntityNotFoundException, DeletedStatusException {
        List<Usage> usageList;

        Query query = entityManager.createQuery(
                "from Usage u where u.loadbalancer.id = :loadBalancerId and u.startTime >= :startTime and u.startTime <= :endTime order by u.startTime asc").setParameter("loadBalancerId", loadBalancerId).setParameter("startTime", startTime).setParameter("endTime", endTime);

        usageList = query.getResultList();

        if (usageList.isEmpty()) {
            return new ArrayList<Usage>();
        }

        return usageList;
    }

    public Collection<AccountBilling> getAccountBillingForAllAccounts(Calendar startTime, Calendar endTime) {
        Query query;
        List<AccountUsage> accountUsageResults;
        List<Usage> lbUsageResults;

        String accountsUsageQuery = "select u from AccountUsage u where "
                + "        u.startTime >= :startTime and "
                + "        u.startTime <= :endTime "
                + "        order by u.accountId asc, u.startTime asc";

        query = entityManager.createQuery(accountsUsageQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        accountUsageResults = query.getResultList();

        String lbUsageQuery = "select u from Usage u where "
                + "        u.startTime >= :startTime  and "
                + "        u.endTime <= :endTime "
                + "        order by u.loadbalancer.accountId asc, u.loadbalancer.id asc, u.startTime asc";

        query = entityManager.createQuery(lbUsageQuery);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        lbUsageResults = query.getResultList();

        return createAccountBillings(accountUsageResults, lbUsageResults);
    }

    private Collection<AccountBilling> createAccountBillings(List<AccountUsage> accountUsageResults,
            List<Usage> lbUsageResults) {
        Map<Integer, AccountBilling> accountBillings = new HashMap<Integer, AccountBilling>();
        Map<Integer, LoadBalancerBilling> loadBalancerBillings = new HashMap<Integer, LoadBalancerBilling>();

        for (AccountUsage accountUsageResult : accountUsageResults) {
            Integer accountId = accountUsageResult.getAccountId();
            AccountBilling accountBilling = getNewOrExistingAccountBilling(accountBillings, accountId);
            accountBilling.getAccountUsageRecords().add(accountUsageResult);
        }

        for (Usage lbUsageResult : lbUsageResults) {
            Integer accountId = lbUsageResult.getLoadbalancer().getAccountId();
            Integer lbId = lbUsageResult.getLoadbalancer().getId();
            String lbName = lbUsageResult.getLoadbalancer().getName();
            AccountBilling accountBilling = getNewOrExistingAccountBilling(accountBillings, accountId);
            LoadBalancerBilling loadBalancerBilling = getNewOrExistingLoadBalancerbilling(loadBalancerBillings, lbId,
                    lbName);
            loadBalancerBilling.getUsageRecords().add(lbUsageResult);
            if (!accountBilling.getLoadBalancerBillings().contains(loadBalancerBilling)) {
                accountBilling.getLoadBalancerBillings().add(loadBalancerBilling);
            }
        }

        return accountBillings.values();
    }

    private AccountBilling getNewOrExistingAccountBilling(Map<Integer, AccountBilling> accountBillings,
            Integer accountId) {
        AccountBilling accountBilling;
        if (accountBillings.containsKey(accountId)) {
            accountBilling = accountBillings.get(accountId);
        } else {
            accountBilling = new AccountBilling();
            accountBilling.setAccountId(accountId);
            accountBillings.put(accountId, accountBilling);
        }
        return accountBilling;
    }

    private LoadBalancerBilling getNewOrExistingLoadBalancerbilling(
            Map<Integer, LoadBalancerBilling> loadBalancerBillings, Integer lbId, String lbName) {
        LoadBalancerBilling loadBalancerBilling;
        if (loadBalancerBillings.containsKey(lbId)) {
            loadBalancerBilling = loadBalancerBillings.get(lbId);
        } else {
            loadBalancerBilling = new LoadBalancerBilling();
            loadBalancerBilling.setLoadBalancerId(lbId);
            loadBalancerBilling.setLoadBalancerName(lbName);
            loadBalancerBillings.put(lbId, loadBalancerBilling);
        }
        return loadBalancerBilling;
    }

    public AccountBilling getAccountBilling(Integer accountId, Calendar startTime, Calendar endTime) throws EntityNotFoundException {
        AccountBilling accountBilling = new AccountBilling();
        accountBilling.setAccountId(accountId);

        Query query;
        List<AccountUsage> accountUsageResults;
        List<Usage> lbUsageResults;

        String accountUsageQuery = "select u from AccountUsage u where u.accountId = :accountId and "
                + "        u.startTime >= :startTime and "
                + "        u.startTime <= :endTime "
                + "        order by u.startTime asc";

        query = entityManager.createQuery(accountUsageQuery);
        query.setParameter("accountId", accountId);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        accountUsageResults = query.getResultList();

        String lbUsageQuery = "select u from Usage u where u.loadbalancer.accountId = :accountId and "
                + "        u.startTime >= :startTime  and "
                + "        u.endTime <= :endTime "
                + "        order by u.loadbalancer.id asc, u.startTime asc";

        query = entityManager.createQuery(lbUsageQuery);
        query.setParameter("accountId", accountId);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        lbUsageResults = query.getResultList();

        accountBilling.setAccountUsageRecords(accountUsageResults);

        Integer currLbId = null;
        LoadBalancerBilling loadbalancerBilling = new LoadBalancerBilling();
        for (Usage usageRecord : lbUsageResults) {
            Integer lbId = usageRecord.getLoadbalancer().getId();
            String lbName = usageRecord.getLoadbalancer().getName();

            if (currLbId == null || !currLbId.equals(lbId)) {
                loadbalancerBilling = new LoadBalancerBilling();
                loadbalancerBilling.setLoadBalancerId(lbId);
                loadbalancerBilling.setLoadBalancerName(lbName);
                accountBilling.getLoadBalancerBillings().add(loadbalancerBilling);
                currLbId = lbId;
            }

            loadbalancerBilling.getUsageRecords().add(usageRecord);
        }

        return accountBilling;
    }

    public HostUsageRecord getHostUsage(Integer hostId, Calendar startTime,
            Calendar endTime) throws EntityNotFoundException {

        Query query;
        List<Object> hostUsageResults;
        List<HostUsage> tst = new ArrayList();

        HostUsageRecord lst = new HostUsageRecord();
        lst.setHostId(hostId);
        String hostUsageQuery = "select sum(bandwidth_in), sum(bandwidth_out), date(start_time) from lb_usage b, loadbalancer a  "
                + "      where a.id = b.loadbalancer_id "
                + "      and a.host_id = :hostId "
                + "      and b.start_time >= :startTime "
                + "      and b.end_time <= :endTime "
                + "      group by date(start_time) desc ";


        query = entityManager.createNativeQuery(hostUsageQuery);
        query.setParameter("hostId", hostId);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);

        hostUsageResults = query.getResultList();


        for (Object r : hostUsageResults) {
            Object[] row = (Object[]) r;

            HostUsage lb = new HostUsage();
            lb.setBandwidthIn(((BigDecimal) row[0]).longValue());
            lb.setBandwidthOut(((BigDecimal) row[1]).longValue());

            Date d = (Date) row[2];
            Calendar l = Calendar.getInstance();
            l.setTime(d);

            lb.setDay(l);

            tst.add(lb);
        }

        lst.setHostUsages(tst);

        return lst;
    }

    public LoadBalancer setLoadBalancerAttrs(LoadBalancer lbIn) throws EntityNotFoundException {
        LoadBalancer lbOut = getById(lbIn.getId());
        if (lbIn.getAccountId() != null) {
            lbOut.setAccountId(lbIn.getAccountId());
        }
        if (lbIn.getAlgorithm() != null) {
            lbOut.setAlgorithm(lbIn.getAlgorithm());
        }
        if (lbIn.getName() != null) {
            lbOut.setName(lbIn.getName());
        }
        if (lbIn.getStatus() != null) {
            lbOut.setStatus(lbIn.getStatus());
        }
        if (lbIn.getProtocol() != null) {
            lbIn.setProtocol(lbIn.getProtocol());
        }
        entityManager.merge(lbOut);
        return lbOut;
    }

    public List<LoadBalancer> getLoadBalancerStatusByTime(int minutesago) {
        Calendar now = Calendar.getInstance();
        Calendar earlier = (Calendar) now.clone();
        earlier.add(Calendar.MINUTE, -minutesago);

        String queryStr = "select lb "
                + "from LoadBalancer lb where lb.status in  ('"
                + LoadBalancerStatus.PENDING_UPDATE
                + "', '"
                + LoadBalancerStatus.BUILD
                + "')"
                + "     and lb.updated <= :now and lb.updated > :earlier "
                + "order by lb.updated desc";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter("now", now);
        query.setParameter("earlier", earlier);
        List results = query.getResultList();
        return results;

    }

    public List<LoadBalancer> getChangesSince(Integer accountId, String status, Integer sinceUnixTime, Integer offset, Integer limit) {
        Calendar now = Calendar.getInstance();
        Calendar changesSince = null;
        Date date = null;
        LoadBalancerStatus lbStatus = null;
        if (status != null) {
            try {
                lbStatus = LoadBalancerStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                lbStatus = null;
            }
        }

        long unixDate = (long) sinceUnixTime;
        String unixFormatted = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ssZ").format(new Date(unixDate * 1000));

        try {

            DateFormat formatter;
            formatter = new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ssZ");
            date = formatter.parse(unixFormatted);

            changesSince = Calendar.getInstance();
            changesSince.setTime(date);
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
        }

        String queryStr = "select lb from LoadBalancer lb where lb.accountId = :accountId"
                + " and lb.updated > :changesSince order by lb.updated desc";

        if (lbStatus != null) {
            queryStr = "select lb from LoadBalancer lb where lb.accountId = :accountId"
                    + " and lb.updated > :changesSince and status = :status order by lb.updated desc";
        }

        Query query = entityManager.createQuery(queryStr);
        query.setParameter("accountId", accountId);
        query.setParameter("changesSince", changesSince);
        if (lbStatus != null) {
            query.setParameter("status", lbStatus);
        }
        query.setFirstResult(offset).setMaxResults(limit);
        List results = query.getResultList();

        return results;
    }

    public List<LoadBalancer> getLoadBalancersStatusAndDate(LoadBalancerStatus error, LoadBalancerStatus build, LoadBalancerStatus pending_update, LoadBalancerStatus pending_delete, Calendar changedSince) throws Exception {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancer> criteria = builder.createQuery(LoadBalancer.class);
        Root<LoadBalancer> lbRoot = criteria.from(LoadBalancer.class);

        if (changedSince == null) {
            changedSince = Calendar.getInstance();


        }
        Predicate errorStatus = builder.equal(lbRoot.get(LoadBalancer_.status), error);
        Predicate buildStatus = builder.equal(lbRoot.get(LoadBalancer_.status), build);
        Predicate pendingUpdateStatus = builder.equal(lbRoot.get(LoadBalancer_.status), pending_update);
        Predicate pendingDeleteStatus = builder.equal(lbRoot.get(LoadBalancer_.status), pending_delete);
        Predicate isBeforeLastUpdated = builder.lessThan(lbRoot.get(LoadBalancer_.updated), changedSince);

        criteria.select(lbRoot);
        criteria.where(builder.and(isBeforeLastUpdated, builder.or(errorStatus, buildStatus, pendingUpdateStatus, pendingDeleteStatus)));


        try {
            List<LoadBalancer> loadBalancers = entityManager.createQuery(criteria).getResultList();
            if (loadBalancers == null) {
                return new ArrayList<LoadBalancer>();
            }
            return loadBalancers;
        } catch (Exception e) {
            LOG.error(e);
            throw new Exception("debug: getLoadBalancerStatusAndDate in LoadbalancerRepository, somthing went wrong...");
        }
    }

    public Boolean getConnectionLoggingbyAccountIdLoadBalancerId(int accId, int lbId) throws EntityNotFoundException {
        String queryStr = "select connectionLogging from LoadBalancer lb where lb.accountId = :accId"
                + " and lb.id = :lbId";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter("accId", accId);
        query.setParameter("lbId", lbId);
        List results = query.getResultList();

        if (results.isEmpty()) {
            throw new EntityNotFoundException("Load balancer not found");
        }

        return (Boolean) results.get(0);
    }

    public void removeRateLimitByExpiration(int id) {
        String queryStr = "delete from RateLimit rl where :now >= rl.expirationTime";

        entityManager.createQuery("delete from RateLimit rl where rl.id = :id").setParameter("id", id).executeUpdate();
    }

    public List<RateLimit> getRateLimitByExpiration() {
        Calendar now = Calendar.getInstance();
        String queryStr = "select rl from RateLimit rl where rl.expirationTime <= :now";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter("now", now);
        List results = query.getResultList();

        return results;
    }

    public void updatePortInJoinTable(LoadBalancer lb) {
        String queryString = "from LoadBalancerJoinVip where loadBalancer.id = :lbId";
        Query query = entityManager.createQuery(queryString).setParameter("lbId", lb.getId());
        LoadBalancerJoinVip loadBalancerJoinVip = (LoadBalancerJoinVip) query.getSingleResult();
        loadBalancerJoinVip.setPort(lb.getPort());
        entityManager.merge(loadBalancerJoinVip);
    }

    public boolean canUpdateToNewPort(Integer newPort, Set<LoadBalancerJoinVip> setToCheckAgainst) {
        Set<VirtualIp> vipsToCheckAgainst = new HashSet<VirtualIp>();

        for (LoadBalancerJoinVip loadBalancerJoinVip : setToCheckAgainst) {
            vipsToCheckAgainst.add(loadBalancerJoinVip.getVirtualIp());
        }

        String queryString = "select j from LoadBalancerJoinVip j where j.virtualIp in (:vips)";
        Query query = entityManager.createQuery(queryString).setParameter("vips", vipsToCheckAgainst);

        List<LoadBalancerJoinVip> entriesWithPortsToCheckAgainst = query.getResultList();

        for (LoadBalancerJoinVip entryWithPortToCheckAgainst : entriesWithPortsToCheckAgainst) {
            if (entryWithPortToCheckAgainst.getPort().equals(newPort)) {
                return false;
            }
        }

        return true;
    }

    public List<Ticket> getTickets(Integer loadBalancerId, Integer... p) {
        Query query;
        String qStr = "SELECT lb.tickets FROM LoadBalancer lb WHERE lb.id = :loadBalancerId";

        query = entityManager.createQuery(qStr).setParameter("loadBalancerId", loadBalancerId);

        if (p.length >= 1) {
            Integer offset = p[0];
            Integer limit = p[1];
            if (offset == null) {
                offset = 0;
            }
            if (limit == null) {
                limit = 100;
            }
            query = query.setFirstResult(offset).setMaxResults(limit);
        }
        return query.getResultList();
    }

    public List<Integer> getAllAccountIds() {
        Query query = entityManager.createQuery("SELECT distinct l.accountId FROM LoadBalancer l");
        return query.getResultList();
    }
}
