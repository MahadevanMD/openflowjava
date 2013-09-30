/* Copyright (C)2013 Pantheon Technologies, s.r.o. All rights reserved. */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.packet.queue.Properties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.packet.queue.PropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.Queues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.queue.get.config.reply.QueuesBuilder;

/**
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class QueueGetConfigReplyMessageFactoryMultiTest {

    /**
     * Testing of {@link QueueGetConfigReplyMessageFactory} for correct
     * translation into POJO
     */
    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 02 03 " + // port
                "00 00 00 00 " + // padding
                "00 00 00 01 " + // queueId
                "00 00 00 01 " + // port
                "00 00 00 00 00 00 00 00 " + // pad
                "00 01 " + // property
                "00 00 00 00 00 00 " + // pad
                "00 00 00 02 " + // queueId
                "00 00 00 02 " + // port
                "00 00 00 00 00 00 00 00 " + // pad
                "00 01 " + // property
                "00 00 00 00 00 00 " + // pad
                "00 00 00 03 " + // queueId
                "00 00 00 03 " + // port
                "00 00 00 00 00 00 00 00 " + // pad
                "00 01 " + // property
                "00 00 00 00 00 00" // pad
        );

        GetQueueConfigOutput builtByFactory = BufferHelper.decodeV13(
                QueueGetConfigReplyMessageFactory.getInstance(), bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertTrue("Wrong port", 66051L == builtByFactory.getPort().getValue());
        Assert.assertTrue("Wrong queues", true == compareLists(builtByFactory.getQueues(),
                        createQueuesList()));
    }

    private static List<Queues> createQueuesList() {
        List<Queues> queuesList = new ArrayList<Queues>();
        QueuesBuilder qb = new QueuesBuilder();
        for (int i = 1; i <= 3; i++) {
            qb.setQueueId(new QueueId((long) i));
            qb.setPort(new PortNumber((long) i));
            qb.setProperties(createPropertiesList());
            queuesList.add(qb.build());
        }
        return queuesList;
    }

    private static List<Properties> createPropertiesList() {
        List<Properties> propertiesList = new ArrayList<Properties>();
        PropertiesBuilder pb = new PropertiesBuilder();
        pb.setProperty(QueueProperty.values()[1]);
        propertiesList.add(pb.build());
        return propertiesList;
    }

    private static boolean compareLists(List<Queues> originalList,
            List<Queues> testList) {
        boolean result = false;
        int originalListLength = originalList.size();
        for (int i = 0; i < originalListLength; i++) {
            if (originalList.get(i).getPort().equals(testList.get(i).getPort())) {
                result = true;
            } else {
                result = false;
                break;
            }
            if (originalList.get(i).getQueueId()
                    .equals(testList.get(i).getQueueId())) {
                result = true;
            } else {
                result = false;
                break;
            }
            if (originalList.get(i).getProperties().get(0).getProperty()
                    .equals(testList.get(i).getProperties().get(0).getProperty())) {
                result = true;
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

}