package club.wodencafe.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.transaction.NotSupportedException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

@Entity
@Table(name = "PLAYER")
@PrimaryKeyJoinColumn
public class Player extends BusinessEntity {

	public Player() {
		super();
		logger.entry();
		logger.trace(getClass().getSimpleName() + " " + getIrcName() + " identityHashCode is ["
				+ System.identityHashCode(this) + "]");
		logger.exit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ircName == null) ? 0 : ircName.hashCode());
		result = prime * result + ((money == null) ? 0 : money.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		logger.entry(obj);
		try {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Player other = (Player) obj;
			if (ircName == null) {
				if (other.ircName != null)
					return false;
			} else if (!ircName.equals(other.ircName))
				return false;
			if (money == null) {
				if (other.money != null)
					return false;
			} else if (!money.equals(other.money))
				return false;
			return true;
		} catch (Throwable th) {
			logger.throwing(th);
			throw new RuntimeException(th);
		}
	}

	private static final XLogger logger = XLoggerFactory.getXLogger(BusinessEntity.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column
	private Long money = 0L;

	@Column
	private String ircName;

	public String getIrcName() {
		logger.entry();
		try {
			return ircName;
		} finally {
			logger.exit(ircName);
		}
	}

	public Long getMoney() {
		logger.entry();
		try {
			return money;
		} finally {
			logger.exit(money);
		}
	}

	public void removeMoney(long money) {
		logger.entry(money);
		try {
			if (money < 0) {
				throw new NotSupportedException("Can't remove negative money");
			}
			this.money -= money;
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(e);
			throw e;
		} finally {
			logger.exit();
		}
	}

	public void setMoney(Long money) {
		logger.entry(money);
		try {
			this.money = money;
		} finally {
			logger.exit();
		}
	}

	public void setIrcName(String ircName) {
		logger.entry(ircName);
		try {
			this.ircName = ircName;
		} finally {
			logger.exit();
		}
	}

	public void addMoney(long money) {
		logger.entry(money);
		try {
			if (money < 0) {
				throw new NotSupportedException(
						"Can't add negative money " + money + " to player " + getIrcName() + ".");
			}
			this.money += money;
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(e);
			throw e;
		}
		logger.exit();
	}

	@Override
	public String toString() {
		logger.entry();
		StringBuilder messageBuilder = new StringBuilder();
		try {
			messageBuilder.append(getIrcName());
			messageBuilder.append("($" + getMoney() + ") ");
			messageBuilder.append("[" + System.identityHashCode(this) + "]");
			return messageBuilder.toString();
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(e);
			throw e;
		} finally {
			logger.exit(messageBuilder.toString());
		}
	}
}
