package org.apache.maven.plugins.war.packaging.refs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.war.Overlay;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.util.ArrayList;
import java.util.List;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
public class DeepReference implements DependencyReference{

    private Overlay overlay;
    private final List<DependencyReference> children = new ArrayList<>();

    private final Artifact classesArtifact;
    private final Artifact warArtifact;
    private final DependencyNode classesNode;
    private final DependencyNode warNode;

    @Override
    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public List<DependencyReference> getChildren() {
        return children;
    }

    @Override
    public void addChildren(List<DependencyReference> children) {
        this.children.addAll(children);
    }

    @Override
    public DependencyNode getNode() {
        return classesNode;
    }

    public DeepReference(Artifact artifact, DependencyNode node, ShallowReference reference, Overlay overlay) {
        if (reference.isWarFile()) {
            classesArtifact = artifact;
            classesNode = node;
            warArtifact = reference.getArtifact(); // No Deps
            warNode = reference.getNode();
            this.overlay = reference.getOverlay();
        } else {
            classesArtifact = reference.getArtifact();
            warArtifact = artifact; // No Deps
            classesNode = reference.getNode();
            warNode = node;
            children.addAll(reference.getChildren());
            this.overlay = overlay;
        }

        if(!(classesArtifact.getGroupId().equals(warArtifact.getGroupId())
            && (classesArtifact.getArtifactId().equals(warArtifact.getArtifactId())))) {
            throw new IllegalArgumentException("Artifacts mismatch "+classesArtifact.getGroupId()+":"+classesArtifact.getArtifactId()+" != "+warArtifact.getGroupId()+":"+warArtifact.getArtifactId());
        }
    }

    public Artifact getArtifact(){
        return classesArtifact;
    }
}
