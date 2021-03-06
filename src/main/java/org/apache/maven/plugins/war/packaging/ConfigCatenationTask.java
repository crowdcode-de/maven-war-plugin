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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.war.Overlay;
import org.apache.maven.plugins.war.util.PathSet;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * Handles an overlay catenation.
 *
 * @author Stephane Nicoll
 */
public class ConfigCatenationTask
    extends AbstractWarPackagingTask
{
    private final Overlay overlay;

    private final boolean isRootPackage;
    private final File destinationDirectory;
    private final String[] includes;


    /**
     * @param overlay {@link #overlay}
     * @param destinationDirectory
     */
    public ConfigCatenationTask( Overlay overlay, boolean isRootPackage, File destinationDirectory,
                                 String... includes )
    {
        this.isRootPackage = isRootPackage;
        this.destinationDirectory = destinationDirectory;
        this.includes = includes;
        if ( overlay == null )
        {
            throw new NullPointerException( "overlay could not be null." );
        }
        this.overlay = overlay;
    }

    @Override
    public void performPackaging( WarPackagingContext context )
            throws MojoExecutionException
    {
        context.getLog()
                .debug( "ConfigCatenationTask: perform catenation overlay.getTargetPath() "
                + overlay.getTargetPath() );
        if ( overlay.shouldSkip() )
        {
            context.getLog().info( "Skipping catenation on  [" + overlay + "]" );
        }
        else
        {
            try
            {
                if (!context.getLog().isDebugEnabled()) {
                    context.getLog().info("Processing catenation on [" + overlay + "]");
                }

                final File tmpDir = context.getWebappSourceDirectory();

                // Step2: setup
                final PathSet includes = getFilesToIncludes( tmpDir, this.includes, null,  false );


                processFiles( overlay, context, tmpDir, includes, this.destinationDirectory );

            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Failed to catenate file from overlay overlay [" + overlay + "]", e );
            }
        }
    }

    /**
     * Copies the files if possible with an optional target prefix.
     * <p>
     * Copy uses a first-win strategy: files that have already been copied by previous tasks are ignored. This method
     * makes sure to update the list of protected files which gives the list of files that have already been copied.
     * <p>
     * If the structure of the source directory is not the same as the root of the webapp, use the <tt>targetPrefix</tt>
     * parameter to specify in which particular directory the files should be copied. Use <tt>null</tt> to copy the
     * files with the same structure
     *
     * @param context        the context to use
     * @param sourceBaseDir  the base directory from which the <tt>sourceFilesSet</tt> will be copied
     * @param sourceFilesSet the files to be copied
     * @throws IOException            if an error occurred while copying the files
     * @throws MojoExecutionException if an error occurs.
     */
    protected void processFiles( Overlay source, WarPackagingContext context, File sourceBaseDir,
                                PathSet sourceFilesSet, File outputFile )
            throws IOException, MojoExecutionException
    {
        try {
            for (String fileToCopyName : sourceFilesSet.paths()) {
                if (!isRootPackage) {
                    final File sourceFile = new File(this.getOverlayTempDirectory(context, overlay), fileToCopyName);
                    if (sourceFile.exists()) {
                        processFile(source, context, sourceFile, outputFile);
                    }
                } else {
                    final File sourceFile = new File( sourceBaseDir, fileToCopyName );
                    processFile( source, context, sourceFile, outputFile );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy the specified file if the target location has not yet already been used.
     * <p>
     * The <tt>targetFileName</tt> is the relative path according to the root of the generated web application.
     *
     * @param source       the source id
     * @param context        the context to use
     * @param file           the file to copy
     * @throws IOException if an error occurred while copying
     */
    // CHECKSTYLE_OFF: LineLength
    protected void processFile( Overlay source, final WarPackagingContext context, final File file, File targetFile )
            throws IOException
    {
        if ( file.isFile() )
        {

            targetFile.getParentFile().mkdirs();

            // Charset for read and write
            Charset charset = StandardCharsets.ISO_8859_1;


            List<String> lines = Files.readAllLines( file.toPath(), charset );

            final Log log = context.getLog();
            if (log.isDebugEnabled()) {
                log.debug(  " ===== CATENATE "
                        + file.getAbsolutePath()
                        + " to " +  targetFile.getAbsolutePath() + "=====" );
                int i = 1;
                for (String line : lines) {
                    log.debug(i++ + ":"+line);
                }
            }

            Files.write( targetFile.toPath(), lines, charset, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND );
        }
    }


    /**
     * Unpacks the specified overlay.
     * 
     * Makes sure to skip the unpack process if the overlay has already been unpacked.
     *
     * @param context the packaging context
     * @param overlay the overlay
     * @return the directory containing the unpacked overlay
     * @throws MojoExecutionException if an error occurred while unpacking the overlay
     */
    protected File unpackOverlay( WarPackagingContext context, Overlay overlay )
        throws MojoExecutionException
    {
        final File tmpDir = getOverlayTempDirectory( context, overlay );

        // TODO: not sure it's good, we should reuse the markers of the dependency plugin
        if ( FileUtils.sizeOfDirectory( tmpDir ) == 0
            || overlay.getArtifact().getFile().lastModified() > tmpDir.lastModified() )
        {
            doUnpack( context, overlay.getArtifact().getFile(), tmpDir );
        }
        else
        {
            context.getLog().debug( "Overlay [" + overlay + "] was already unpacked" );
        }
        return tmpDir;
    }

    /**
     * Returns the directory to use to unpack the specified overlay.
     *
     * @param context the packaging context
     * @param overlay the overlay
     * @return the temp directory for the overlay
     */
    protected File getOverlayTempDirectory( WarPackagingContext context, Overlay overlay )
    {
        final File groupIdDir = new File( context.getOverlaysWorkDirectory(), overlay.getGroupId() );
        if ( !groupIdDir.exists() )
        {
            groupIdDir.mkdir();
        }
        String directoryName = overlay.getArtifactId();
        if ( overlay.getClassifier() != null )
        {
            directoryName = directoryName + "-" + overlay.getClassifier();
        }
        final File result = new File( groupIdDir, directoryName );
        if ( !result.exists() )
        {
            result.mkdirs();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigCatenationTask that = (ConfigCatenationTask) o;

        return this.overlay.getGroupId().equals(that.overlay.getGroupId())
                && this.overlay.getArtifactId().equals(that.overlay.getArtifactId());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return this.overlay.getGroupId()+":"+ this.overlay.getArtifactId();
    }
}
