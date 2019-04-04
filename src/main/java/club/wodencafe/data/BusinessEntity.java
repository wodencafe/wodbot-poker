package club.wodencafe.data;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(name = "BUSINESS_ENTITY")
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners({ BusinessListener.class })
public class BusinessEntity implements Serializable {

	@Column(name = "BUSINESS_ENTITY_ID", updatable = false, nullable = false)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
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

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Version
	@Nonnull
	@Column(columnDefinition = "integer DEFAULT 0", nullable = false)
	private long version = 0L;
}