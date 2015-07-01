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
