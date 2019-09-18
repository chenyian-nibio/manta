package jp.go.nibiohn.bioinfo.shared;

import java.sql.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SampleEntry implements IsSerializable {

	private String sampleId;
	private Date createDate;
	private String[] displayColumns;
	private String[] columnValue;
	private Boolean hasReads;
	
	public SampleEntry() {
	}

	public SampleEntry(String sampleId, Date createDate, String[] displayColumns, String[] columnValue,
			Boolean hasReads) {
		super();
		this.sampleId = sampleId;
		this.createDate = createDate;
		this.displayColumns = displayColumns;
		this.columnValue = columnValue;
		this.hasReads = hasReads;
	}

	public String getSampleId() {
		return sampleId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Boolean hasReads() {
		return hasReads;
	}

	public String[] getDisplayColumns() {
		return displayColumns;
	}

	public String[] getColumnValue() {
		return columnValue;
	}

	public Boolean getHasReads() {
		return hasReads;
	}

}
