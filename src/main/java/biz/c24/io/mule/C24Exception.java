/*
 * Copyright C24 Technologies Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.c24.io.mule;

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
