package org.openstack.atlas.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

public class UpdateErrorFileListener extends BaseListener {
    private final Log LOG = LogFactory.getLog(UpdateErrorFileListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
        String msg = String.format("Inside %s.doMessage", this.getClass().getName());
        LOG.info(msg);
        MessageDataContainer data = getDataContainerFromMessage(message);
        String content = data.getErrorFileContents();
        Integer aid = data.getAccountId();
        Integer lid = data.getLoadBalancerId();
        Integer clusterId = data.getClusterId();
        if (aid != null && lid != null) {
            try {
                LOG.debug("Attempting to set error file in zeus...calling setErrorFile");
                reverseProxyLoadBalancerService.setErrorFile(lid, aid, content);
                LOG.debug("Successfully updated error file in zeus.");
            } catch (Exception e) {
                String tmpMsg = String.format("Error setting Errorfile for %d_%d", aid, lid);
                LOG.error(tmpMsg, e);
                notificationService.saveAlert(aid, lid, e, AlertType.ZEUS_FAILURE.name(), msg);
            }
        } else if (clusterId != null) {
            LOG.debug("Attempting to upload default error file in zeus...calling uploadDefaultErrorFile");
            try {
                reverseProxyLoadBalancerService.uploadDefaultErrorFile(clusterId, content);
                LOG.debug("Successfully uploaded default error file in zeus.");
            } catch (Exception e) {
                String tmpMsg = String.format("Error uploading default error file...");
                LOG.error(tmpMsg, e);
                notificationService.saveAlert(null, null, e, AlertType.ZEUS_FAILURE.name(), msg);
            }
        }
    }
}
