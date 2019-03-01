package jp.go.nibiohn.bioinfo.shared;

import java.sql.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SampleEntry implements IsSerializable {

	private String sampleId;
	private Integer age;
	private String gender;
	private String project;
	private Date expDate;
	private Boolean hasReads;
	
	public SampleEntry() {
	}

	public SampleEntry(String sampleId, Integer age, String gender, Date expDate, Boolean hasReads) {
		super();
		this.sampleId = sampleId;
		this.age = age;
		this.gender = gender;
		this.expDate = expDate;
		this.hasReads = hasReads;
	}
	
	public SampleEntry(String sampleId, Integer age, String gender, String project, Date expDate, Boolean hasReads) {
		super();
		this.sampleId = sampleId;
		this.age = age;
		this.gender = gender;
		this.project = project;
		this.expDate = expDate;
		this.hasReads = hasReads;
	}

	public String getSampleId() {
		return sampleId;
	}

	public Integer getAge() {
		return age;
	}

	public String getGender() {
		return gender;
	}
	
	public String getProject() {
		return project;
	}

	public Date getExpDate() {
		return expDate;
	}

	public Boolean hasReads() {
		return hasReads;
	}

	public void setProject(String project) {
		this.project = project;
	}
	
}
