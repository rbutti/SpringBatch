package com.rave.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemReader;
import javax.batch.operations.BatchRuntimeException;
import javax.inject.Inject;

import org.beanio.BeanReader;
import org.beanio.BeanReaderIOException;
import org.beanio.InvalidRecordException;
import org.beanio.StreamFactory;
import org.beanio.UnidentifiedRecordException;

/**
 * An implementation of {@code javax.batch.api.chunk.ItemReader} based on BeanIO. 
 * This reader class handles all data formats that are supported by BeanIO. 
 * Such data formats are fixed length files, CSV files, and XML. It supports 
 * BeanIO mappings and provides restart capability.
 * 
 * <p>All that needs to be done to utilize this class is to define
 * BeanIO reader in the job definition for a particular step in a job.</p>
 * 
 * <p>
 * This BeanIO item reader supports injection of the following properties, as 
 * defined in the job specification language for a particular batch job:
 * 	<ul>	
 * 		<li><b>filePath</b> - the name of the file to read from - 
 * 			the file path must be specified 
 * 		</li> 
 * 		<li><b>streamName</b> - Name of the BeanIO stream defined in 
 * 			BeanIO mapping file. It must be specified.
 * 		</li>
 * 		<li><b>mappingFile</b> - Location of the BeanIO mapping file. 
 * 			It must be specified.
 * 		</li>
 * 		<li><b>encoding</b> - encoding to be used for the read file. 
 * 			It is optional. Default encoding is "UTF-8"
 * 		</li> 
 * 		<li><b>errorHandlerClass</b> - errorHandlerClass to be used for defining. 
 * 			the BeanReader error handler. Default error handler class is:
 * 			 com.deloitte.common.batch.BeanIOItemReaderErrorHandler
 * 		</li> 
 * 	</ul>
 */
public final class BeanIOItemReader implements ItemReader {
	
	/**
	 * Default Encoding for the file
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";
	

    /**
     * The name of the file to read from
     */
    @Inject
    @BatchProperty
    private String filePath;

    /**
     * Name of the BeanIO stream defined in BeanIO mapping file
     */
    @Inject
    @BatchProperty
    private String streamName;

    /**
     * Location of the BeanIO mapping file
     */
    @Inject
    @BatchProperty
    private String mappingFile;
    
	/**
	 * Encoding for the input file. If not provided, default is UTF-8
	 */
	@Inject
	@BatchProperty
	private String encoding;
	

    private BeanReader reader;
	private long items = 0;
    
	public BeanIOItemReader() { 
		super();
	}
	

	
	/**
	 * Constructor that instantiates an instance of the BeanIOItemReader
	 * 
	 * @param filePath represents the path and name of the file to read from
	 * 
	 * @param streamName represents BeanIO streaming name as defined 
	 * in the BeanIO mapping file
	 * 
	 * @param mappingFile represents BeanIO mapping file
	 * 
	 * @param encoding represents encoding of the input file
	 * 
	 * @param errorHandlerClass represents the error handler class name
	 */
	public BeanIOItemReader(String filePath, String streamName, String mappingFile, 
			String encoding) {
		
		this.filePath = filePath;
		this.streamName = streamName;
		this.mappingFile = mappingFile;
		this.encoding = (this.encoding == null) ? DEFAULT_ENCODING : encoding;
		
	}
    
	/* (non-Javadoc)
	 * @see javax.batch.api.chunk.ItemReader#open(java.io.Serializable)
	 */
	@Override
	public void open(Serializable checkpoint) throws Exception {
		
		if(filePath == null) {
			throw new IllegalArgumentException("File path cannot be empty");
		}
		if(streamName == null) {
			throw new IllegalArgumentException("BeanIO stream name cannot be empty");
		}
		if(encoding == null) {
			encoding = DEFAULT_ENCODING;
		}
		
		
		// BufferedReader to be processed, as defined by the filePath and encoding
		BufferedReader readerForFileProcessing = this.openFileForProcessing();
		
		// BeanIO mappingFile file
		String mappingFilePath = this.getMappingFilePath();

		final StreamFactory streamFactory = StreamFactory.newInstance();
		streamFactory.load(mappingFilePath);
		reader = streamFactory.createReader(this.streamName, readerForFileProcessing);
		
		// In case of restarting, reset position to last read point
		if(checkpoint != null) {
			if(!Number.class.isInstance(checkpoint)) {				
				throw new BatchRuntimeException("Unexpected checkpoint exception. " +
						"The BeanIOItemReader checkpoint must be a number, reflecting the last item processed" +
						"when checkpoint was taken.");
			}
			items = Number.class.cast(checkpoint).longValue();
			int skipped = reader.skip((int)items);
			
			if(skipped < items) {
				throw new IllegalStateException("Failed to skip "+ items +" item, end of stream reached after " +
						skipped + " items");
			}
		}
		
		
	}

	/**
	 * The readItem method returns the next item for chunk processing
	 * @see javax.batch.api.chunk.ItemReader#readItem(). In the case of this
	 * implementation, it reads a single record from the file input using the 
	 * BeanIO stream. If the end of the stream is reached, null is returned.
     * 
     * @return next item or null
     * @throws UnidentifiedRecordException - 
     * 				thrown when the record type of the last record read from 
     * 				a BeanReader could not be determined
     * @throws InvalidRecordException - 
     * 				thrown when a record or one of its fields does not pass validation during unmarshalling
	 */
	@Override
	public Object readItem() throws InvalidRecordException, UnidentifiedRecordException {
		
		final Object item = reader.read();
		if(item != null) {
			incrementReaderCount();
		}
		
		return item;
	}
	
	/* (non-Javadoc)
	 * @see javax.batch.api.chunk.ItemReader#checkpointInfo()
	 */
	@Override
	public Serializable checkpointInfo() throws Exception {
		return this.items;
	}

	/* (non-Javadoc)
	 * @see javax.batch.api.chunk.ItemReader#close()
	 */
	@Override
	public void close() throws Exception {
		
		if(reader != null) {
			try {
				// closes this and underlying output stream
				reader.close();
			} catch(BeanReaderIOException e) {
				throw new BatchRuntimeException("Unable to close the the BatchIOItemReader", e);
			} finally {
				reader = null;
			}
		}
	}

	/**
	 * Increment the count of items by one 
	 */
	private void incrementReaderCount() {
		items += 1;
	}
	
	/**
	 * This method validates if the input mappingFile exists and returns the URI path of the mappingFile. 
	 * 
	 * @return String or null
	 * @throws URISyntaxException
	 */
	private String getMappingFilePath() throws URISyntaxException {
		
		String mappingFilePath = null;
		if(!new File(this.mappingFile).exists()) {
			final URL mappingFileUrl = this.getClass().getClassLoader().getResource(this.mappingFile);
			if(mappingFileUrl != null && mappingFileUrl.getPath() != null) {
				mappingFilePath = mappingFileUrl.toURI().getPath();
			} else {
				throw new BatchRuntimeException("BeanIO mapping file does not exist!");
			}
		}
		return mappingFilePath;
	}
	
	/**
	 * This method returns an java.io.File object corresponding to a physical File specified by the 
	 * filePath variable. If the physical file doesn't exist a BatchRuntimeException is thrown
	 * 
	 * @return File
	 * @throws URISyntaxException
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	private BufferedReader openFileForProcessing() throws URISyntaxException, 
			UnsupportedEncodingException, FileNotFoundException {
		
		File fileForProfessing = new File(this.filePath);
		if(!fileForProfessing.exists()) {
			final URL fileForProfessingUrl = this.getClass().getClassLoader().getResource(this.filePath);
			if(fileForProfessingUrl != null && fileForProfessingUrl.getPath() != null) {
				fileForProfessing = new File(fileForProfessingUrl.toURI().getPath());
			} else {
				throw new BatchRuntimeException("File for processing does not exist!");
			}
		}

		return new BufferedReader(new InputStreamReader(new FileInputStream(fileForProfessing), this.encoding));
	}
}
