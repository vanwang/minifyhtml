package com.puma.minifyhtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Apply compression on JS and CSS (using YUI Compressor).
 *
 * @goal compress
 * @phase process-resources
 *
 * @threadSafe
 */
public class MinifyHtmlCompressorMojo extends MojoSupport {

    /**
     * Read the input file using "encoding".
     *
     * @parameter expression="${file.encoding}" default-value="UTF-8"
     */
    private String encoding;


    /**
     * No compression
     *
     * @parameter expression="${maven.minifyhtml.nocompress}" default-value="false"
     */
    private boolean nocompress;
    
    /**
     * request to create a gzipped version of the minifyhtml/aggregation files.
     *
     * @parameter expression="${maven.minifyhtml.gzip}" default-value="false"
     */
    private boolean gzip;
    
    /**
     * a list of aggregation/concatenation to do after processing,
     * for example to create big js files that contain several small js files.
     * Aggregation could be done on any type of file (js, css, ...).
     *
     * @parameter
     */
    private Aggregation[] aggregations;


    /**
     * show statistics (compression ratio).
     *
     * @parameter expression="${maven.minifyhtml.statistics}" default-value="true"
     */
    private boolean statistics;

    /**
     * aggregate files before minify
     * @parameter expression="${maven.minifyhtml.preProcessAggregates}" default-value="false"
     */
    private boolean preProcessAggregates;

    private long inSizeTotal_;
    private long outSizeTotal_;

    @Override
    protected String[] getDefaultIncludes() throws Exception {
        return new String[]{"**/*.ftl", "**/*.html"};
    }

    @Override
    public void beforeProcess() throws Exception {

        if(preProcessAggregates) aggregate();
    }

    @Override
    protected void afterProcess() throws Exception {
        if (statistics && (inSizeTotal_ > 0)) {
            getLog().info(String.format("total input (%db) -> output (%db)[%d%%]", inSizeTotal_, outSizeTotal_, ((outSizeTotal_ * 100)/inSizeTotal_)));
        }

        if(!preProcessAggregates) aggregate();
    }

    private void aggregate() throws Exception {
        if (aggregations != null) {
            for(Aggregation aggregation : aggregations) {
                getLog().info("generate aggregation : " + aggregation.output);
                aggregation.run();
                File gzipped = gzipIfRequested(aggregation.output);
                if (statistics) {
                    if (gzipped != null) {
                        getLog().info(String.format("%s (%db) -> %s (%db)[%d%%]", aggregation.output.getName(), aggregation.output.length(), gzipped.getName(), gzipped.length(), ratioOfSize(aggregation.output, gzipped)));
                    } else if (aggregation.output.exists()){
                        getLog().info(String.format("%s (%db)", aggregation.output.getName(), aggregation.output.length()));
                    } else {
                        getLog().warn(String.format("%s not created", aggregation.output.getName()));
                    }
                }
            }
        }
    }

    @Override
    protected void processFile(SourceFile src) throws Exception {
        if (getLog().isDebugEnabled()) {
            getLog().debug("compress file :" + src.toFile()+ " to " + src.toDestFile(""));
        }
        File inFile = src.toFile();
        File outFile = src.toDestFile("");

        getLog().debug("only compress if input file is younger than existing output file");

        InputStreamReader in = null;
        OutputStreamWriter out = null;
        File outFileTmp = new File(outFile.getAbsolutePath() + ".tmp");
        FileUtils.forceDelete(outFileTmp);
        try {
            in = new InputStreamReader(new FileInputStream(inFile), encoding);
            if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException( "Cannot create resource output directory: " + outFile.getParentFile() );
            }
            getLog().debug("use a temporary outputfile (in case in == out)");

            getLog().debug("start compression");
            out = new OutputStreamWriter(new FileOutputStream(outFileTmp), encoding);
            if (nocompress) {
                getLog().info("No compression is enabled");
                IOUtil.copy(in, out);
            }
            else {
                compressHtml(in, out);
            }
            getLog().debug("end compression");
        } finally {
            IOUtil.close(in);
            IOUtil.close(out);
        }
        FileUtils.forceDelete(outFile);
        FileUtils.rename(outFileTmp, outFile);
        File gzipped = gzipIfRequested(outFile);
        if (statistics) {
            inSizeTotal_ += inFile.length();
            outSizeTotal_ += outFile.length();
            getLog().info(String.format("%s (%db) -> %s (%db)[%d%%]", inFile.getName(), inFile.length(), outFile.getName(), outFile.length(), ratioOfSize(inFile, outFile)));
            if (gzipped != null) {
                getLog().info(String.format("%s (%db) -> %s (%db)[%d%%]", inFile.getName(), inFile.length(), gzipped.getName(), gzipped.length(), ratioOfSize(inFile, gzipped)));
            }
        }
    }

	private void compressHtml(InputStreamReader in, OutputStreamWriter out)
			throws IOException {
		try{
			PumaHtmlCompressor compressor = new PumaHtmlCompressor(in);
			compressor.compress(out);
		}catch(IllegalArgumentException e){
			throw new IllegalArgumentException(
					"Unexpected characters found in html or ftl file.",e);
		}
	}

    protected File gzipIfRequested(File file) throws Exception {
        if (!gzip || (file == null) || (!file.exists())) {
            return null;
        }
        if (".gz".equalsIgnoreCase(FileUtils.getExtension(file.getName()))) {
            return null;
        }
        File gzipped = new File(file.getAbsolutePath()+".gz");
        getLog().debug(String.format("create gzip version : %s", gzipped.getName()));
        GZIPOutputStream out = null;
        FileInputStream in = null;
        try {
            out = new GZIPOutputStream(new FileOutputStream(gzipped));
            in = new FileInputStream(file);
            IOUtil.copy(in, out);
        } finally {
            IOUtil.close(in);
            IOUtil.close(out);
        }
        return gzipped;
    }

    protected long ratioOfSize(File file100, File fileX) throws Exception {
        long v100 = Math.max(file100.length(), 1);
        long vX = Math.max(fileX.length(), 1);
        return (vX * 100)/v100;
    }
}
