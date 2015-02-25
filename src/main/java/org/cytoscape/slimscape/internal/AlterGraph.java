package org.cytoscape.slimscape.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AlterGraph {
    CyApplicationManager manager;
    List<String> uniprotIDs;
    List<String> upc;
    CyEventHelper eventHelper;
    CyNetworkManager networkManager;
    CyNetworkViewFactory networkViewFactory;
    VisualMappingManager visualMappingManager;

    public AlterGraph (List<String> uniprotIDs, List<String> occNodes, List<String> upc, CyApplicationManager manager, CyEventHelper eventHelper,
                       CyNetworkFactory networkFactory, CyNetworkManager networkManager,
                       CyNetworkViewFactory networkViewFactory, CyNetworkViewManager networkViewManager, VisualMappingManager visualMappingManager) {
        this.uniprotIDs = uniprotIDs;
        this.upc = upc;
        this.manager = manager;
        this.eventHelper = eventHelper;
        this.networkManager = networkManager;
        this.networkViewFactory = networkViewFactory;
        this.visualMappingManager = visualMappingManager;

        // Attempts to alter the preexisting graph. If that fails, makes a new one.
        try {
            CyNetwork network = manager.getCurrentNetwork();
            // Get state of graph
            List<CyNode> nodes = CyTableUtil.getNodesInState(network, "selected", true);

            // Adds selected nodes to a Map
            Map<String, CyNode> nodeIds = new HashMap<String, CyNode>();
            for (CyNode node : nodes) {
                String name = network.getRow(node).get(CyNetwork.NAME, String.class);
                if (!nodeIds.containsKey(name)) {
                    nodeIds.put(name, node);
                }
            }

            List<String> occ = new ArrayList<String>();
            for (int i=0; i<occNodes.size(); i++) {
                String id = occNodes.get(i).split("_")[3];
                occ.add(id);
            }

            SLiMNodeStyle(occ, nodeIds, manager, visualMappingManager);

        } catch (Exception e){ // No network, need to make a new one
            CyNetwork newNetwork = networkFactory.createNetwork();
            newNetwork.getRow(newNetwork).set(CyNetwork.NAME, "SLiMOutput");
            networkManager.addNetwork(newNetwork);

            Map<String, CyNode> nodeIds = new HashMap<String, CyNode>();

            try {
                // Creates the graph
                addNodes(uniprotIDs, nodeIds, newNetwork, networkViewManager, manager);
                SLiMNodeStyle(occNodes, nodeIds, manager, visualMappingManager);
                addUpcConnections(upc, nodeIds, newNetwork);

                networkManager.addNetwork(newNetwork);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }

        }
    }

    /**
     * @desc - Adds nodes to the protein network from the information returned by the Slim* run.
     * @param uniprotIDs - list of all Uniprot IDs input to the returned run.
     * @param nodeIds - map linking all selected Uniprot IDs to their CyNodes, for easy access to the network.
     * @param newNetwork - CyNetwork of the network being altered.
     * @param networkViewManager - NetworkViewManager for the network being altered. Initialised in CyActivator.
     * @param manager - CyApplicationManager for the network being altered. Initialised in CyActivator.
     */
    public void addNodes (List<String> uniprotIDs, Map<String, CyNode> nodeIds, CyNetwork newNetwork,
                          CyNetworkViewManager networkViewManager, CyApplicationManager manager) {
        for (String id : uniprotIDs) {
            if (!nodeIds.containsKey(id)) {
                CyNode node = newNetwork.addNode();
                newNetwork.getDefaultNodeTable().getRow(node.getSUID()).set("name", id.split("_")[3]);
                nodeIds.put(id, node);
            }
        }
        eventHelper.flushPayloadEvents();

        // Add network view
        final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(newNetwork);
        CyNetworkView myView = null;
        if(views.size() != 0) {
            myView = views.iterator().next();
        }

        if (myView == null) {
            // create a new view for my network
            myView = networkViewFactory.createNetworkView(newNetwork);
            networkViewManager.addNetworkView(myView);
        } else {
            System.out.println("networkView already existed.");
        }

        CyNetworkView networkView =  manager.getCurrentNetworkView();
        Iterator it = nodeIds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            CyNode node = (CyNode) pairs.getValue();
            View<CyNode> nodeView = networkView.getNodeView(node);
            nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
            nodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
            nodeView.setLockedValue(BasicVisualLexicon.NODE_SIZE, 60.0);
        }
    }

    /**
     * @desc - Alters the visual style of the nodes which are known to contain SLiMs.
     * @param occNodes - List of Uniprot IDs of proteins known to contain SLiMs.
     * @param nodeIds - map linking all selected Uniprot IDs to their CyNodes, for easy access to the graph.
     * @param manager - CyApplicationManager for the network being altered. Initialised in CyActivator.
     * @param visualMappingManager - VisualMappingManager for the network. Initialised in CyActivator.
     */
    public void SLiMNodeStyle (List<String> occNodes, Map<String, CyNode> nodeIds, CyApplicationManager manager,
                               VisualMappingManager visualMappingManager) {
        CyNetworkView networkView =  manager.getCurrentNetworkView();

        List<CyNode> nodes = new ArrayList<CyNode>();
        for (String id : occNodes) {
            if (nodeIds.containsKey(id)) {
                CyNode node = nodeIds.get(id); // We should have the node here
                nodes.add(node);
            }
        }

        Iterator<CyNode> it = nodes.iterator();
        while (it.hasNext()) {
            CyNode node = it.next();
            View<CyNode> nodeView = networkView.getNodeView(node);

            nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.RED);
            nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.DIAMOND);
            nodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
            nodeView.setLockedValue(BasicVisualLexicon.NODE_SIZE, 60.0);
        }

        VisualStyle style = visualMappingManager.getCurrentVisualStyle();
        style.apply(networkView);
        networkView.updateView();
    }

    /**
     * @desc - Function to add UPC connections to a Cytoscape network.
     * @param upc - list of strings consisting of \s separated lists of connected nodes; drawn from upc output of slim*.
     * @param nodeIds - map linking Uniprot IDs to their CyNodes, for easy access to the network.
     * @param newNetwork - CyNetwork of the network being altered.
     */
    public void addUpcConnections (List<String> upc, Map<String, CyNode> nodeIds, CyNetwork newNetwork) {
        for(String line : upc) {
            String[] elements = line.split("\\s");
            for (int a=0; a<elements.length-1; a++) {
                for (int b=a+1; b<elements.length; b++) {
                    if (a != b) {
                        CyNode node1 = nodeIds.get(elements[a]);
                        CyNode node2 = nodeIds.get(elements[b]);

                        if (node1 == null) {
                            CyNode node = newNetwork.addNode();
                            newNetwork.getRow(node).set(CyNetwork.NAME, elements[a]);
                            nodeIds.put(elements[a], node);
                        }
                        if (node2 == null) {
                            CyNode node = newNetwork.addNode();
                            newNetwork.getRow(node).set(CyNetwork.NAME, elements[b]);
                            nodeIds.put(elements[b], node);
                        }
                        newNetwork.addEdge(node1, node2, true);

                    }
                }
            }
        }
        eventHelper.flushPayloadEvents();
    }

}