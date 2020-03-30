package org.apache.maven.plugins.war.packaging;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;

/**
 * Handles an overlay catenation.
 *
 * @author Stephane Nicoll
 */
public class FinishCatenationTask
    extends AbstractWarPackagingTask
{

    private final File tmpFile;
    private final File outFile;

    public FinishCatenationTask(File tmpFile, File outFile) {
        this.tmpFile = tmpFile;
        this.outFile = outFile;
    }

    @Override
    public void performPackaging(WarPackagingContext context) throws MojoExecutionException, MojoFailureException {
        try {
            copyFile(context, tmpFile, outFile, outFile.getName(), false);
            tmpFile.delete();
        } catch (IOException e) {
            throw new MojoExecutionException("Finishing the catenation task failed", e);
        }
    }
}
