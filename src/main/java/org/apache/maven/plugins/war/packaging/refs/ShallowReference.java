package org.apache.maven.plugins.war.packaging.refs;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.war.Overlay;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.util.ArrayList;
import java.util.Collection;
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
public class ShallowReference implements DependencyReference {

    private final Overlay overlay;
    private final Artifact artifact;
    private final DependencyNode node;
    private final List<DependencyReference> children  = new ArrayList<>();

    public ShallowReference(Overlay overlay, Artifact artifact, DependencyNode node) {
        this.overlay = overlay;
        this.artifact = artifact;
        this.node = node;
    }

    @Override
    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public List<DependencyReference> getChildren() {
        return children;
    }

    public boolean isWarFile() {
        return artifact.getType().equals("war");
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public boolean add(DependencyReference dependencyReference) {
        return children.add(dependencyReference);
    }

    public boolean addAll(Collection<? extends DependencyReference> c) {
        return children.addAll(c);
    }

    public DependencyNode getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShallowReference that = (ShallowReference) o;

        return this.artifact.getGroupId().equals(that.artifact.getGroupId())
                && this.artifact.getArtifactId().equals(that.artifact.getArtifactId());
    }

    @Override
    public void addChildren(List<DependencyReference> children) {
        this.children.addAll(children);
    }

}
