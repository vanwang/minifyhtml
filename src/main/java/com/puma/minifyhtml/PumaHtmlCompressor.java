package com.puma.minifyhtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor;

public class PumaHtmlCompressor {

	private StringBuffer srcsb = new StringBuffer();
	
	public PumaHtmlCompressor(Reader in) throws IOException{
		 // Read the stream...
        int c;
        while ((c = in.read()) != -1) {
            srcsb.append((char) c);
        }
	}
	
	public void compress(Writer out) throws IOException {  

		String html = srcsb.toString();
		
		HtmlCompressor compressor = new HtmlCompressor();

		compressor.setEnabled(true);                   //if false all compression is off (default is true)
		compressor.setRemoveComments(true);            //if false keeps HTML comments (default is true)
		compressor.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
		compressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
		compressor.setRemoveQuotes(false);              //removes unnecessary tag attribute quotes
		compressor.setSimpleDoctype(true);             //simplify existing doctype
		compressor.setRemoveScriptAttributes(false);    //remove optional attributes from script tags
		compressor.setRemoveStyleAttributes(false);     //remove optional attributes from style tags
		compressor.setRemoveLinkAttributes(false);      //remove optional attributes from link tags
		compressor.setRemoveFormAttributes(false);      //remove optional attributes from form tags
		compressor.setRemoveInputAttributes(false);     //remove optional attributes from input tags
		compressor.setSimpleBooleanAttributes(false);   //remove values from boolean tag attributes
		compressor.setRemoveJavaScriptProtocol(false);  //remove "javascript:" from inline event handlers
		compressor.setRemoveHttpProtocol(false);        //replace "http://" with "//" inside tag attributes
		compressor.setRemoveHttpsProtocol(false);       //replace "https://" with "//" inside tag attributes
		compressor.setPreserveLineBreaks(false);        //preserves original line breaks

		compressor.setCompressCss(true);               //compress inline css 
		compressor.setCompressJavaScript(true);        //compress inline javascript
		compressor.setYuiCssLineBreak(-1);             //--line-break param for Yahoo YUI Compressor 
		compressor.setYuiJsDisableOptimizations(false); //--disable-optimizations param for Yahoo YUI Compressor 
		compressor.setYuiJsLineBreak(-1);              //--line-break param for Yahoo YUI Compressor 
		compressor.setYuiJsNoMunge(true);              //--nomunge param for Yahoo YUI Compressor 
		compressor.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor 
		compressor.setRemoveIntertagSpaces(true);

		//use Google Closure Compiler for javascript compression
		//compressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor(CompilationLevel.WHITESPACE_ONLY));
		compressor.setJavaScriptCompressor(new YuiJavaScriptCompressor());

		compressor.setCssCompressor(new YuiCssCompressor());

		String compressedHtml = compressor.compress(html);

         // Write the output...
         out.write(compressedHtml);
	}  
	
	public static void main(String[] args)throws Exception {  
//		System.out.println(getStr("d:/a.txt"));
		File inFile = new File("D:\\test\\test.html");
		File outFile = new File("D:\\test\\test-minified.html");
		File outFileTmp = new File(outFile.getAbsolutePath() + ".tmp");
		
		 InputStreamReader in = null;
	     OutputStreamWriter out = null;
	     
	     in = new InputStreamReader(new FileInputStream(inFile), "UTF-8");
	     
	     out = new OutputStreamWriter(new FileOutputStream(outFileTmp), "UTF-8");
	     
	     new PumaHtmlCompressor(in).compress(out);
	     
	     IOUtil.close(in);
         IOUtil.close(out);
         
         FileUtils.forceDelete(outFile);
         FileUtils.rename(outFileTmp, outFile);
	}
	
}
