package uky.icmpdrop;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.ICMP;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.utils.IPProtocols;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Drop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*Interface IListenDataPacket is the one that handles all packet-in messages. To do so,
 * we need to define the function receiveDataPacket()*/
public class PacketHandler implements IListenDataPacket {
 
    private static final Logger log = LoggerFactory.getLogger(PacketHandler.class);
    private IDataPacketService dataPacketService; // object used to parse raw packets
    private IFlowProgrammerService flowService; // object used to program flows
    private short hardTimeOut = 10; // The flows will expire after 10 seconds
 
    /*
     * Sets a reference to the requested DataPacketService
     * See Activator class in configureInstance method. 
     */
    void setDataPacketService(IDataPacketService s) {
        log.trace("Set DataPacketService.");
 
        dataPacketService = s;
    }
    /* Same as above*/
    void setFlowProgrammerService(IFlowProgrammerService s) {
        log.trace("Set Flow Programmer Service");
 
        flowService = s;
    }
    /*
     * Likewise, unsets DataPacketService. See Activator class as well. 
     */
    void unsetDataPacketService(IDataPacketService s) {
        log.trace("Removed DataPacketService.");
 
        if (dataPacketService == s) {
            dataPacketService = null;
        }
    }
    
    void unsetFlowProgrammerService(IFlowProgrammerService f) {
        log.trace("Removed FlowProgrammerService.");
 
        if (flowService == f) {
            flowService = null;
        }
    }
 
   
    /* Packet-in messages are handled within this method which receives a RawPacket
     * as parameter. */
    @Override
    public PacketResult receiveDataPacket(RawPacket inPkt) {
        log.trace("Received data packet.");
        
        // From which port did the packet come from?
        NodeConnector ingressConnector = inPkt.getIncomingNodeConnector();
        // Which switch (node) received the packet (Connector above belongs to that node)? 
        Node node = ingressConnector.getNode();
        // Decode the incoming packet
        Packet l2pkt = dataPacketService.decodeDataPacket(inPkt);

 
        /* Checks if packet is Ethernet (other packets are valid as well such as ARP)*/
        if (l2pkt instanceof Ethernet) {
        	// Extract the payload and check if it contains an ICMP packet. 
            Packet l3Pkt = l2pkt.getPayload();
            if (l3Pkt instanceof ICMP) {
                ICMP icmpPkt = (ICMP) l3Pkt;
                /* Get the checksum and type of control message */
                short ChkSum = icmpPkt.getChecksum();
                byte type = icmpPkt.getType();
                /* Print out those values on standard output*/
                System.out.println("ICMP packet received: Checksum " + ChkSum + " - Type of Control Message: " + type );
                /* Create the flow entry that will drop following ICMP packets for 10 seconds*/
                Match match = new Match();
                match.setField(MatchType.NW_PROTO,IPProtocols.ICMP.byteValue()); //1 = ICMP, 6 = TCP and 17=UDP

                List<Action> actions = new LinkedList<Action>();
                actions.add(new Drop());
                // A flow entry is made up of a pattern match and a set of actions to be performed. 
                Flow fl = new Flow(match,actions);
                fl.setHardTimeout(hardTimeOut);
                Status status = flowService.addFlow(node,fl);
                if (!status.isSuccess()){
                	log.error("Error while creating flow entry: " + status.getDescription());
                	return PacketResult.CONSUME;
                }
                //Don't do anything else with the packet and prevent other handlers to use it.
                // This can be changed of course. 
                return PacketResult.CONSUME;
            } else {
            	System.out.println("Received another type of packet from node: " + node.getNodeIDString());
            	System.out.println("Payload - " + l3Pkt.toString());
            }
        }
        // Do not process the packet. Maybe another handler will do it 
        return PacketResult.IGNORED;
    }
}