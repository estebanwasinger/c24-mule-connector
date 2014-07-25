/**
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.c24;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
import org.mule.config.i18n.Message;


public class C24Exception extends MessagingException {

    private static final long serialVersionUID = 1146047707127690570L;

    public C24Exception(Message message, MuleEvent event, MessageProcessor failingMessageProcessor)
    {
        super(message, event, failingMessageProcessor);
    }

    public C24Exception(Message message,
                        MuleEvent event,
                        Throwable cause,
                        MessageProcessor failingMessageProcessor)
    {
        super(message, event, cause, failingMessageProcessor);
    }

    public C24Exception(Message message, MuleEvent event, Throwable cause)
    {
        super(message, event, cause);
    }

    public C24Exception(Message message, MuleEvent event)
    {
        super(message, event);
    }

    public C24Exception(MuleEvent event, Throwable cause, MessageProcessor failingMessageProcessor)
    {
        super(event, cause, failingMessageProcessor);
    }

    public C24Exception(MuleEvent event, Throwable cause)
    {
        super(event, cause);
    }


}
