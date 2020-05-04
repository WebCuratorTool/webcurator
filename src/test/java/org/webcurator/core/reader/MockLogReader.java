package org.webcurator.core.reader;

public class MockLogReader extends LogReaderImpl {

	private String baseDir = "/org/webcurator/core/reader/logs";
	
	public MockLogReader()
	{
		super.setLogProvider(new MockLogProvider(baseDir));
	}
	
	public void setLogProvider(LogProvider provider)
	{
	}
}
