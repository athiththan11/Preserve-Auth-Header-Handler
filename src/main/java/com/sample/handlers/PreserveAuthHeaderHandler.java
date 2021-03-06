package com.sample.handlers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;

/**
 * Custom handler implementation to extract and preserve the Authorization
 * Header in the Message Context to re-use
 */
public class PreserveAuthHeaderHandler extends AbstractHandler {

    private String authorizationHeader;
    private static final Log log = LogFactory.getLog(PreserveAuthHeaderHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        preserveAuthHeader(messageContext);
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    /**
     * Extract authorization header from Transport Headers and set it to Message
     * Context with custom properties to reuse in the mediation
     * 
     * @param context Message Context
     */
    @SuppressWarnings("unchecked")
    private void preserveAuthHeader(MessageContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Extracting Auth Header: " + authorizationHeader);
        }

        org.apache.axis2.context.MessageContext axis2Context = ((Axis2MessageContext) context).getAxis2MessageContext();
        Map<String, Object> transportHeaders = (Map<String, Object>) axis2Context
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String tokenContent;
        if (StringUtils.isNotBlank(authorizationHeader) && transportHeaders.containsKey(authorizationHeader)) {
            tokenContent = (String) transportHeaders.get(authorizationHeader);

            if (log.isDebugEnabled()) {
                log.debug("Extracted authorization header: " + authorizationHeader + " with value: " + tokenContent);
            }

            context.setProperty("PRESERVE_AUTH_HEADER_HANDLER_HEADER", authorizationHeader);
            context.setProperty("PRESERVE_AUTH_HEADER_HANDLER_TOKEN", tokenContent);
        }
    }

    public void setAuthorizationHeader(String authHeader) {
        this.authorizationHeader = authHeader;
    }
}
