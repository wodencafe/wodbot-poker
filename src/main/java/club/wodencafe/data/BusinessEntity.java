package club.wodencafe.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name="BUSINESS_ENTITY")
@Inheritance(strategy=InheritanceType.JOINED)
public class BusinessEntity {

	@Column(name = "BUSINESS_ENTITY_ID")
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable=false, updatable=false)
	private Date createdDate;
	
	@Column
	private Date modifiedDate;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	@PrePersist
	void onCreate() {
		this.setCreatedDate(new Date());
	}
	@PreUpdate
	void onPersist() {
		this.setModifiedDate(new Date());
	}

}