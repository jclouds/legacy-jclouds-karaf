/*
 * Copyright (C) 2011, the original authors
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.karaf.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.extensions.ImageExtension;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Location;
import org.jclouds.scriptbuilder.domain.Statement;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A proxy implementations of the {@link ComputeService} which delegates calls to the underlying impl and notifies
 * {@link NodeListener}s about node creation/destruction events.
 */
public class ComputeServiceEventProxy implements ComputeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeServiceEventProxy.class);

    private BundleContext bundleContext;
    private ComputeService computeService;

    private ServiceTracker listenerTracker;

    /**
     * Constructor
     *
     * @param bundleContext
     * @param computeService
     */
    public ComputeServiceEventProxy(BundleContext bundleContext, ComputeService computeService) {
        this.bundleContext = bundleContext;
        this.computeService = computeService;
        this.listenerTracker = new ServiceTracker(bundleContext, NodeListener.class.getName(), null);
        this.listenerTracker.open();
    }

    private List<? extends NodeListener> getNodeListeners() {
        List<NodeListener> listeners = new LinkedList<NodeListener>();
        try {
            listeners.addAll(Arrays.asList((NodeListener[]) listenerTracker.getServices()));
        } catch (Exception ex) {
            LOGGER.warn("Could not lookup node listeners. Listeners will not receive the last event.");
        }
        return listeners;
    }

    /**
     * @return a reference to the context that created this ComputeService.
     */
    @Override
    public ComputeServiceContext getContext() {
        return computeService.getContext();
    }

    /**
     * Makes a new template builder for this service
     */
    @Override
    public TemplateBuilder templateBuilder() {
        return computeService.templateBuilder();
    }

    /**
     * Makes a new set of options for running nodes
     */
    @Override
    public TemplateOptions templateOptions() {
        return computeService.templateOptions();
    }

    /**
     * The list hardware profiles command shows you the options including virtual cpu count, memory,
     * and disks. cpu count is not a portable quantity across clouds, as they are measured
     * differently. However, it is a good indicator of relative speed within a cloud. memory is
     * measured in megabytes and disks in gigabytes.
     *
     * @return a map of hardware profiles by ID, conceding that in some clouds the "id" is not used.
     */
    @Override
    public Set<? extends Hardware> listHardwareProfiles() {
        return computeService.listHardwareProfiles();
    }

    /**
     * Images define the operating system and metadata related to a node. In some clouds, Images are
     * bound to a specific region, and their identifiers are different across these regions. For this
     * reason, you should consider matching image requirements like operating system family with
     * TemplateBuilder as opposed to choosing an image explicitly. The getImages() command returns a
     * map of images by id.
     */
    @Override
    public Set<? extends Image> listImages() {
        return computeService.listImages();
    }

    /**
     * all nodes available to the current user by id. If possible, the returned set will include
     * {@link org.jclouds.compute.domain.NodeMetadata} objects.
     */
    @Override
    public Set<? extends ComputeMetadata> listNodes() {
        return computeService.listNodes();
    }

    /**
     * The list locations command returns all the valid locations for nodes. A location has a scope,
     * which is typically region or zone. A region is a general area, like eu-west, where a zone is
     * similar to a datacenter. If a location has a parent, that implies it is within that location.
     * For example a location can be a rack, whose parent is likely to be a zone.
     */
    @Override
    public Set<? extends Location> listAssignableLocations() {
        return computeService.listAssignableLocations();
    }

    /**
     * The compute api treats nodes as a group based on the name you specify. Using this group, you
     * can choose to operate one or many nodes as a logical unit without regard to the implementation
     * details of the cloud.
     * <p/>
     * <p/>
     * The set that is returned will include credentials you can use to ssh into the nodes. The "key"
     * part of the credentials is either a password or a private key. You have to inspect the value
     * to determine this.
     * <p/>
     * <pre>
     * if (node.getCredentials().key.startsWith("-----BEGIN RSA PRIVATE KEY-----"))
     *    // it is a private key, not a password.
     * </pre>
     * <p/>
     * <p/>
     * Note. if all you want to do is execute a script at bootup, you should consider use of the
     * runscript option.
     * <p/>
     * If resources such as security groups are needed, they will be reused or created for you.
     * Inbound port 22 will always be opened up.
     *
     * @param group    - common identifier to group nodes by, cannot contain hyphens
     * @param count    - how many to fire up.
     * @param template - how to configure the nodes
     * @return all of the nodes the api was able to launch in a running state.
     * @throws org.jclouds.compute.RunNodesException
     *          when there's a problem applying options to nodes. Note that successful and failed
     *          nodes are a part of this exception, so be sure to inspect this carefully.
     */
    @Override
    public Set<? extends NodeMetadata> createNodesInGroup(String group, int count, Template template) throws RunNodesException {
        Set<? extends NodeMetadata> metadata = computeService.createNodesInGroup(group, count, template);
        try {
            for (NodeListener listener : getNodeListeners()) {
                for (NodeMetadata node : metadata) {
                    listener.nodeCreated(node);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error while notifying node listeners.", ex);
        }
        return metadata;
    }

    /**
     * Like {@link org.jclouds.compute.ComputeService#createNodesInGroup(String, int, org.jclouds.compute.domain.Template)}, except that the template
     * is default, equivalent to {@code templateBuilder().any().options(templateOptions)}.
     */
    @Override
    public Set<? extends NodeMetadata> createNodesInGroup(String group, int count, TemplateOptions templateOptions) throws RunNodesException {
        Set<? extends NodeMetadata> metadata = computeService.createNodesInGroup(group, count, templateOptions);
        try {
            for (NodeListener listener : getNodeListeners()) {
                for (NodeMetadata node : metadata) {
                    listener.nodeCreated(node);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error while notifying node listeners.", ex);
        }
        return metadata;
    }

    /**
     * Like {@link org.jclouds.compute.ComputeService#createNodesInGroup(String, int, org.jclouds.compute.options.TemplateOptions)}, except that the
     * options are default, as specified in {@link org.jclouds.compute.ComputeService#templateOptions}.
     */
    @Override
    public Set<? extends NodeMetadata> createNodesInGroup(String group, int count) throws RunNodesException {
        Set<? extends NodeMetadata> metadata = computeService.createNodesInGroup(group, count);
        try {
            for (NodeListener listener : getNodeListeners()) {
                for (NodeMetadata node : metadata) {
                    listener.nodeCreated(node);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error while notifying node listeners.", ex);
        }
        return metadata;
    }

    /**
     * resume the node from {@link org.jclouds.compute.domain.NodeState#SUSPENDED suspended} state,
     * given its id.
     * <p/>
     * <h4>note</h4>
     * <p/>
     * affected nodes may not resume with the same IP address(es)
     */
    @Override
    public void resumeNode(String id) {
        computeService.resumeNode(id);
    }

    /**
     * nodes matching the filter are treated as a logical set. Using the resume command, you can save
     * time by resumeing the nodes in parallel.
     * <p/>
     * <h4>note</h4>
     * <p/>
     * affected nodes may not resume with the same IP address(es)
     *
     * @throws UnsupportedOperationException if the underlying provider doesn't support suspend/resume
     * @throws java.util.NoSuchElementException
     *                                       if no nodes matched the predicate specified
     */
    @Override
    public void resumeNodesMatching(Predicate<NodeMetadata> filter) {
        computeService.resumeNodesMatching(filter);
    }

    /**
     * suspend the node, given its id. This will result in
     * {@link org.jclouds.compute.domain.NodeState#SUSPENDED suspended} state.
     * <p/>
     * <h4>note</h4>
     * <p/>
     * affected nodes may not resume with the same IP address(es)
     *
     * @throws UnsupportedOperationException if the underlying provider doesn't support suspend/resume
     */
    @Override
    public void suspendNode(String id) {
        computeService.suspendNode(id);
    }

    /**
     * nodes matching the filter are treated as a logical set. Using the suspend command, you can
     * save time by suspending the nodes in parallel.
     * <p/>
     * <h4>note</h4>
     * <p/>
     * affected nodes may not resume with the same IP address(es)
     *
     * @throws UnsupportedOperationException if the underlying provider doesn't support suspend/resume
     * @throws java.util.NoSuchElementException
     *                                       if no nodes matched the predicate specified
     */
    @Override
    public void suspendNodesMatching(Predicate<NodeMetadata> filter) {
        computeService.suspendNodesMatching(filter);
    }

    /**
     * destroy the node, given its id. If it is the only node in a tag set, the dependent resources
     * will also be destroyed.
     */
    @Override
    public void destroyNode(String id) {
        NodeMetadata node = null;
        try {
            node = computeService.getNodeMetadata(id);
        } catch (Exception ex) {
            LOGGER.error("Error while retrieving node metadata.", ex);
        }
        computeService.destroyNode(id);
        try {
            for (NodeListener listener : getNodeListeners()) {
                listener.nodeDestroyed(node);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while notifying node listeners.", ex);
        }

    }

    /**
     * nodes matching the filter are treated as a logical set. Using the delete command, you can save
     * time by removing the nodes in parallel. When the last node in a set is destroyed, any indirect
     * resources it uses, such as keypairs, are also destroyed.
     *
     * @return list of nodes destroyed
     */
    @Override
    public Set<? extends NodeMetadata> destroyNodesMatching(Predicate<NodeMetadata> filter) {
        Set<? extends NodeMetadata> metadata = computeService.destroyNodesMatching(filter);
        try {
            for (NodeListener listener : getNodeListeners()) {
                for (NodeMetadata node : metadata) {
                    listener.nodeCreated(node);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error while notifying node listeners.", ex);
        }
        return metadata;
    }

    /**
     * reboot the node, given its id.
     */
    @Override
    public void rebootNode(String id) {
        computeService.rebootNode(id);
    }

    /**
     * nodes matching the filter are treated as a logical set. Using this command, you can save time
     * by rebooting the nodes in parallel.
     *
     * @throws java.util.NoSuchElementException
     *          if no nodes matched the predicate specified
     */
    @Override
    public void rebootNodesMatching(Predicate<NodeMetadata> filter) {
        computeService.rebootNodesMatching(filter);
    }

    /**
     * Find a node by its id.
     */
    @Override
    public NodeMetadata getNodeMetadata(String id) {
        return computeService.getNodeMetadata(id);
    }

    /**
     * get all nodes including details such as image and ip addresses even if it incurs extra
     * requests to the service.
     *
     * @param filter how to select the nodes you are interested in details on.
     */
    @Override
    public Set<? extends NodeMetadata> listNodesDetailsMatching(Predicate<ComputeMetadata> filter) {
        return computeService.listNodesDetailsMatching(filter);
    }

    /**
     * @see org.jclouds.compute.ComputeService#runScriptOnNodesMatching(com.google.common.base.Predicate, org.jclouds.scriptbuilder.domain.Statement, org.jclouds.compute.options.RunScriptOptions)
     */
    @Override
    public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(Predicate<NodeMetadata> filter, String runScript) throws RunScriptOnNodesException {
        return computeService.runScriptOnNodesMatching(filter, runScript);
    }

    /**
     * @see org.jclouds.compute.ComputeService#runScriptOnNodesMatching(com.google.common.base.Predicate, org.jclouds.scriptbuilder.domain.Statement, org.jclouds.compute.options.RunScriptOptions)
     */
    @Override
    public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(Predicate<NodeMetadata> filter, Statement runScript) throws RunScriptOnNodesException {
        return computeService.runScriptOnNodesMatching(filter, runScript);
    }

    /**
     * @see org.jclouds.compute.ComputeService#runScriptOnNodesMatching(com.google.common.base.Predicate, org.jclouds.scriptbuilder.domain.Statement, org.jclouds.compute.options.RunScriptOptions)
     */
    @Override
    public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(Predicate<NodeMetadata> filter, String runScript, RunScriptOptions options) throws RunScriptOnNodesException {
        return computeService.runScriptOnNodesMatching(filter, runScript, options);
    }

    /**
     * Run the script on all nodes with the specific predicate.
     *
     * @param filter    Predicate-based filter to define on which nodes the script is to be executed
     * @param runScript statement containing the script to run
     * @param options   nullable options to how to run the script, whether to override credentials
     * @return map with node identifiers and corresponding responses
     * @throws java.util.NoSuchElementException
     *          if no nodes matched the predicate specified
     * @throws org.jclouds.compute.RunScriptOnNodesException
     *          if anything goes wrong during script execution
     * @see org.jclouds.compute.predicates.NodePredicates#runningInGroup(String)
     * @see org.jclouds.scriptbuilder.domain.Statements
     */
    @Override
    public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(Predicate<NodeMetadata> filter, Statement runScript, RunScriptOptions options) throws RunScriptOnNodesException {
        return computeService.runScriptOnNodesMatching(filter, runScript, options);
    }

    /**
     * Run the script on a specific node
     *
     * @param id        node the script is to be executed on
     * @param runScript statement containing the script to run
     * @param options   nullable options to how to run the script, whether to override credentials
     * @return map with node identifiers and corresponding responses
     * @throws java.util.NoSuchElementException
     *                               if the node is not found
     * @throws IllegalStateException if the node is not in running state
     * @throws org.jclouds.compute.callables.ScriptStillRunningException
     *                               if the script was still running after {@link org.jclouds.compute.reference.ComputeServiceConstants.Timeouts#scriptComplete}
     * @see org.jclouds.compute.predicates.NodePredicates#runningInGroup(String)
     * @see org.jclouds.scriptbuilder.domain.Statements
     */
    @Override
    public ExecResponse runScriptOnNode(String id, Statement runScript, RunScriptOptions options) {
        return computeService.runScriptOnNode(id, runScript, options);
    }

    /**
     * Run the script on a specific node in the background, typically as {@code nohup}
     *
     * @param id        node the script is to be executed on
     * @param runScript statement containing the script to run
     * @param options   nullable options to how to run the script, whether to override credentials
     * @return map with node identifiers and corresponding responses
     * @throws java.util.NoSuchElementException
     *                               if the node is not found
     * @throws IllegalStateException if the node is not in running state
     * @see org.jclouds.compute.predicates.NodePredicates#runningInGroup(String)
     * @see org.jclouds.scriptbuilder.domain.Statements
     */
    @Override
    @Beta
    public ListenableFuture<ExecResponse> submitScriptOnNode(String id, Statement runScript, RunScriptOptions options) {
        return computeService.submitScriptOnNode(id, runScript, options);
    }

    /**
     * @see #runScriptOnNode(String, org.jclouds.scriptbuilder.domain.Statement, org.jclouds.compute.options.RunScriptOptions)
     */
    @Override
    public ExecResponse runScriptOnNode(String id, Statement runScript) {
        return computeService.runScriptOnNode(id, runScript);
    }

    /**
     * @see #runScriptOnNode(String, org.jclouds.scriptbuilder.domain.Statement, org.jclouds.compute.options.RunScriptOptions)
     * @see org.jclouds.scriptbuilder.domain.Statements#exec
     */
    @Override
    public ExecResponse runScriptOnNode(String id, String runScript, RunScriptOptions options) {
        return computeService.runScriptOnNode(id, runScript, options);
    }

    /**
     * @see #runScriptOnNode(String, String, org.jclouds.compute.options.RunScriptOptions)
     */
    @Override
    public ExecResponse runScriptOnNode(String id, String runScript) {
        return computeService.runScriptOnNode(id, runScript);
    }

  @Override
  public Optional<ImageExtension> getImageExtension() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
