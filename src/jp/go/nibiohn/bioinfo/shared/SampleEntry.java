package jp.go.nibiohn.bioinfo.shared;

import java.sql.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SampleEntry implements IsSerializable {

	private String sampleId;
	private Integer age;
	private String gender;
	private String project;
	private Date expDate;
	private boolean hasMetadata;
	private boolean has16S;
	private boolean hasShotgun;
	
	public SampleEntry() {
	}

	public SampleEntry(String sampleId, Integer age, String gender, Date expDate, Boolean hasMetadata, Boolean has16s,
			Boolean hasShotgun) {
		super();
		this.sampleId = sampleId;
		this.age = age;
		this.gender = gender;
		this.expDate = expDate;
		this.hasMetadata = hasMetadata;
		this.has16S = has16s;
		this.hasShotgun = hasShotgun;
	}

	public SampleEntry(String sampleId, Integer age, String gender, String project, Date expDate, Boolean hasMetadata,
			Boolean has16s, Boolean hasShotgun) {
		super();
		this.sampleId = sampleId;
		this.age = age;
		this.gender = gender;
		this.project = project;
		this.expDate = expDate;
		this.hasMetadata = hasMetadata;
		this.has16S = has16s;
		this.hasShotgun = hasShotgun;
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

	public boolean hasMetadata() {
		return hasMetadata;
	}

	public boolean has16SData() {
		return has16S;
	}

	public boolean hasShotgunData() {
		return hasShotgun;
	}

}
