package club.wodencafe.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "PLAYER")
@PrimaryKeyJoinColumn
public class Player extends BusinessEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column
	private Long money = 0L;

	@Column
	private String ircName;

	public String getIrcName() {
		return ircName;
	}

	public Long getMoney() {
		return money;
	}

	public void removeMoney(long money) {
		if (money < 0) {
			throw new RuntimeException("Can't remove negative money");
		}
		this.money -= money;
	}

	public void setMoney(Long money) {
		this.money = money;
	}

	public void setIrcName(String ircName) {
		this.ircName = ircName;
	}

	public void addMoney(long money) {
		if (money < 0) {
			throw new RuntimeException("Can't add negative money");
		}
		this.money += money;
	}

	@Override
	public String toString() {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(getIrcName());
		messageBuilder.append("($" + getMoney() + ") ");
		messageBuilder.append("[" + System.identityHashCode(this) + "]");
		return messageBuilder.toString();
	}
}
