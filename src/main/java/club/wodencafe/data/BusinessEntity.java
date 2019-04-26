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

	public BusinessEntity() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((modifiedDate == null) ? 0 : modifiedDate.hashCode());
		result = prime * result + (int) (version ^ (version >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BusinessEntity other = (BusinessEntity) obj;
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modifiedDate == null) {
			if (other.modifiedDate != null)
				return false;
		} else if (!modifiedDate.equals(other.modifiedDate))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

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